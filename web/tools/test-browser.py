#!/usr/bin/env python3
"""Real browser + HTTP/Java parser fixture; no production account or private capture."""
import sys,tempfile,threading,os,json,time,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];sys.path[:0]=[str(ROOT),str(ROOT/'tests')]
from test_backend import Website,event
from server import App,Handler,ThreadingHTTPServer,now_ms,encode
from playwright.sync_api import sync_playwright
os.environ['ALLOW_LOCAL_TEST']='1';os.environ['WEBSITE_HOSTS']='127.0.0.1'
site=ThreadingHTTPServer(('127.0.0.1',0),Website);threading.Thread(target=site.serve_forever,daemon=True).start()
Website.rows=[event('old','Điện áp cảm biến 7 < 5V',at=now_ms()-20000),event('old2','Chuông 1 hỏng',point='Km 100+000',at=now_ms()-10000),event('train','Tàu lẻ',point='Km 100+000',at=now_ms()-4000,red=False)]
tmp=tempfile.TemporaryDirectory();app=App(tmp.name,start_worker=False)
server=ThreadingHTTPServer(('127.0.0.1',0),Handler);server.app=app;threading.Thread(target=server.serve_forever,daemon=True).start()
base='http://127.0.0.1:%s'%server.server_port
out=ROOT/'docs/screenshots';out.mkdir(parents=True,exist_ok=True)
try:
 with sync_playwright() as p:
  engines=[('chromium',lambda:p.chromium.launch(executable_path=os.environ.get('CHROMIUM_PATH') or shutil.which('chromium'),args=['--no-sandbox']))]
  if os.environ.get('TEST_WEBKIT')=='1':engines.append(('webkit',lambda:p.webkit.launch()))
  for name,launch in engines:
   browser=launch();context=browser.new_context(viewport={'width':390,'height':844},device_scale_factor=1,is_mobile=True,has_touch=True);page=context.new_page();errors=[];page.on('pageerror',lambda e:errors.append(str(e)))
   page.goto(base);page.locator('#site-url').fill('http://127.0.0.1:%s/'%site.server_port);page.locator('#site-user').fill('fixture');page.locator('#site-password').fill('local-test-password');page.locator('#save').click();page.locator('#summary-title').filter(has_text='CÓ SỰ CỐ').wait_for()
   def refresh():page.evaluate('document.dispatchEvent(new Event("visibilitychange"))');page.wait_for_timeout(800)
   assert page.locator('#confirm').is_visible();assert not page.locator('#retry').is_visible()
   page.route('**/api/state*',lambda route:route.abort());refresh();assert page.locator('#retry').is_visible();assert 'Mất kết nối' in page.locator('#connection').inner_text();page.unroute('**/api/state*');page.locator('#retry').click();page.wait_for_timeout(800);assert not page.locator('#retry').is_visible()
   assert page.locator('#summary').bounding_box()['y']<page.locator('#connection').bounding_box()['y']<page.locator('#diagram-scroll').bounding_box()['y']
   for width in [320,375,390,430]:
    page.set_viewport_size({'width':width,'height':844});page.wait_for_timeout(200);assert page.evaluate('document.documentElement.scrollWidth<=innerWidth'),str(width)+' overflow'
    page.locator('#toast').evaluate('(e)=>e.hidden=true');page.screenshot(path=str(out/(name+'-overview-'+str(width)+'.png')))
   with app.db() as db:mid=db.execute('SELECT id FROM monitors').fetchone()[0]
   before=json.loads(app.monitor(mid)['state']);before.update(connectionLost=True,status='Mất kết nối tới website.')
   with app.db() as db:db.execute('UPDATE monitors SET state=? WHERE id=?',(encode(before),mid))
   refresh();assert page.locator('#retry').is_visible();page.locator('#retry').click();page.wait_for_timeout(500);assert app.monitor(mid)['next_due']<=time.time();page.screenshot(path=str(out/(name+'-connection-lost.png')))
   app.announce(mid,before,before['faults']);page.locator('#confirm').click();page.wait_for_timeout(500);assert len(json.loads(app.monitor(mid)['state'])['faults'])==2
   session=list(context.cookies())[0]['value'];s,_=app.session(session);assert json.loads(s['options'])['running'];assert json.loads(s['options'])['confirmedThrough']>0
   # Requirements 1.2: marker changes even with unchanged route/train segments.
   assert page.locator('[data-point="100+000"] circle').get_attribute('fill')=='#ff5b6e'
   assert page.locator('[data-point="102+000 (Vị)"] rect').nth(1).get_attribute('fill')=='#fbbf24'
   assert page.locator('#route path[stroke="#35d3a5"]').count()>0
   retained=json.loads(app.monitor(mid)['state'])
   def replace_state(value):
    with app.db() as db:db.execute('UPDATE monitors SET state=? WHERE id=?',(encode(value),mid))
    refresh()
   replace_state(dict(retained,faults=[]))
   assert page.locator('[data-point="100+000"] circle').get_attribute('fill')!='#ff5b6e'
   replace_state(retained)
   assert page.locator('[data-point="100+000"] circle').get_attribute('fill')=='#ff5b6e'
   page.locator('.navigation [data-go=crossings]').click();page.wait_for_timeout(800)
   assert '1 sự cố' in page.locator('#crossings-list [data-crossing="101+000"]').inner_text()
   assert 'Tốt' in page.locator('#crossings-list [data-crossing="280+500"]').inner_text()
   with app.db() as db:db.execute('DELETE FROM days WHERE monitor=?',(mid,))
   refresh()
   assert 'Tốt' not in page.locator('#crossings-list [data-crossing="280+500"]').inner_text()
   with app.db() as db:db.execute('INSERT INTO days VALUES(?,?)',(mid,__import__('server').today().isoformat()))
   refresh()
   page.locator('.navigation [data-go=crossings]').click();page.locator('#crossing-filter').select_option('fault');assert page.locator('#crossings-list [data-crossing]').count()==2;page.locator('#crossing-filter').select_option('clear');assert page.locator('#crossings-list [data-crossing]').count()==1;page.locator('#crossing-filter').select_option('all');page.screenshot(path=str(out/(name+'-crossings.png')));page.locator('#crossings-list [data-crossing="101+000"]').click();page.locator('#device-groups .device').first.wait_for();page.wait_for_timeout(1200)
   assert page.locator('#device-groups').inner_text().count('Cảm biến')>=1
   assert 'Điện áp cảm biến 7 < 5V' in page.locator('#device-groups').inner_text();page.screenshot(path=str(out/(name+'-detail.png')))
   page.locator('.navigation [data-go=incidents]').click();page.wait_for_timeout(800);page.screenshot(path=str(out/(name+'-incidents.png')));page.locator('.navigation [data-go=settings]').click();page.screenshot(path=str(out/(name+'-settings.png')));assert page.locator('#site-password').input_value()==''
   assert page.locator('[data-action=stream]').count()==0;assert '10 giây' in page.locator('#page-settings').inner_text();assert page.locator('label.radio').all_text_contents()==['Giọng nói','Chỉ chuông','Im lặng']
   page.locator('input[value=silent]').check();page.locator('#save').click();page.wait_for_timeout(1000)
   s,_=app.session(session);assert json.loads(s['options'])['mode']=='silent'
   # Reload returns cached faults but does not include old notification audio.
   page.reload();page.wait_for_timeout(700);assert not errors,errors
   print('PASS',name,'real DOM, 4 mobile widths, layout, retry, confirm, device groups, settings, reload; no JS exceptions')
   browser.close()
finally:
 app.stop.set();app.pool.shutdown();server.shutdown();server.server_close();site.shutdown();site.server_close();tmp.cleanup()
