"""Web 1.2 requirements, deliberately separate from unmodified Android tests."""
import unittest,tempfile,json,sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from server import App,now_ms,ZONE,today
from test_backend import event
from datetime import datetime

class WebRules(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory();self.app=App(self.tmp.name,start_worker=False)
        self.now=now_ms();self.start=int(datetime.combine(today(),datetime.min.time(),ZONE).timestamp()*1000)
    def tearDown(self):self.app.pool.shutdown();self.tmp.cleanup()
    def analyze(self,rows,**kw):
        req=dict(op='analyze',now=self.now,route=['100+000','101+000','102+000'],events=rows,previous={})
        req.update(kw);return self.app.engine(req)
    def count(self,id,deltas,point='Km 101+000',at=None):
        counts=[str(40+deltas.get(cb,0)) for cb in range(1,7)]
        return dict(event(id,'Khôi phục hệ thống',point,at or self.start+1000,False),counts=counts)
    def test_small_deltas_raw_preserved_all_derived_absent(self):
        rows=[self.count('small'+str(d),{1:d}) for d in (-2,-1,1,2)]
        old=dict(event('axle:1:small1','CB1 đếm sai',at=self.start+1000),counts=rows[0]['counts'])
        s=self.analyze(rows+[old],historical=True)
        self.assertEqual(s['axles'],[]);self.assertFalse(any(e['id'].startswith('axle:') for e in s['records']))
        self.assertEqual({e['id']:e['counts'] for e in s['records']},{e['id']:e['counts'] for e in rows})
        self.assertEqual(s['faults'],[]);self.assertEqual(s['devices'],{})
    def test_mixed_sensors_totals_and_sixth_only(self):
        rows=[self.count('m'+str(i),{1:2,2:3},at=self.start+(i+1)*1000) for i in range(7)]
        s=self.analyze(rows,historical=True);self.assertEqual({a['sensor'] for a in s['axles']},{2})
        last=s['axles'][-1];self.assertEqual((last['dailyCount'],last['extra'],last['missing']),(7,21,0))
        self.assertEqual([e['id'] for e in s['faults']],['axle:2:m5'])
    def test_large_new_source_double_threshold_and_boundaries(self):
        rows=[self.count('b'+str(i),{1:3 if i<5 else 10},at=self.start+(i+1)*1000) for i in range(7)]
        rows.append(self.count('nine',{2:-9,3:-2},at=self.start+9000))
        s=self.analyze(rows,historical=True)
        self.assertEqual({e['id'] for e in s['faults']},{'axle:1:b5','axle:1:b6'})
        self.assertEqual([a['delta'] for a in s['axles'] if a['sensor']==2],[-9])
        self.assertFalse(any(a['sensor']==3 for a in s['axles']))
    def test_day_crossing_sensor_and_duplicate_isolation(self):
        rows=[self.count('p'+str(i),{1:3},at=self.start+(i+1)*1000) for i in range(5)]
        rows += [self.count('other',{1:3},point='Km 100+000'),self.count('prior',{1:3},at=self.start-1000),rows[0]]
        s=self.analyze(rows,historical=True);self.assertFalse(s['faults'])
        self.assertEqual(max(a['dailyCount'] for a in s['axles']),5)
    def test_six_exemptions_keep_hardware_raw(self):
        points=['280+500','307+507','309+056','320+445','338+900','343+350']
        rows=[]
        for p in points:rows += [self.count(p,{1:20},point='Km '+p),event('fault'+p,point='Km '+p,at=self.start+2000)]
        s=self.analyze(rows,route=points,historical=True)
        self.assertEqual(s['axles'],[]);self.assertEqual(len(s['faults']),6);self.assertEqual(len(s['records']),12)
    def test_self_success_after_ten_late_evidence_and_offline(self):
        arrival=event('arrival','Tàu lẻ',at=self.now-1000,red=False)
        s=self.analyze([arrival],previous={'lastSuccess':self.now-10000},added=['arrival'])
        self.assertIn('arrival',s['pending'])
        for elapsed in (9999,10000,50000):
            offline=self.analyze([arrival],now=self.now+elapsed,previous=dict(s,lastSuccess=self.now),maintenance=True)
            self.assertFalse(offline['derived'])
        early=self.analyze([arrival],now=self.now+9999,previous=dict(s,lastSuccess=self.now));self.assertFalse(early['derived'])
        yes=self.analyze([arrival],now=self.now+10000,previous=dict(s,lastSuccess=self.now));self.assertEqual([e['id'] for e in yes['derived']],['self:arrival'])
        proof=event('proof','Tàu lẻ',point='Km 100+000',at=self.now-2000,red=False)
        corrected=self.analyze([proof,arrival]+yes['derived'],now=self.now+20000,previous=dict(yes,lastSuccess=self.now+10000),added=['proof'])
        self.assertIn('self:arrival',corrected['discard']);self.assertFalse(corrected['faults'])

if __name__=='__main__':unittest.main()
