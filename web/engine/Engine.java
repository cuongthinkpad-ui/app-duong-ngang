package vn.duongngang.monitor;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import org.json.*;

/** Headless adapter; uses a web rule variant; Android originals remain in reference, without Android stubs. */
public final class Engine {
  static Object json(Object o) throws Exception {
    if (o == null) return JSONObject.NULL;
    if (o instanceof Map<?,?> map) { var out=new JSONObject(); for(var e:map.entrySet()) out.put(e.getKey().toString(),json(e.getValue())); return out; }
    if (o instanceof Collection<?> items) { var out=new JSONArray(); for(var item:items) out.put(json(item)); return out; }
    if (o.getClass().isRecord()) { var out=new JSONObject(); for(var c:o.getClass().getRecordComponents()) out.put(c.getName(),json(c.getAccessor().invoke(o))); return out; }
    return o;
  }
  static List<Rules.Event> events(JSONArray rows) {
    var out=new ArrayList<Rules.Event>();
    for(Object item:rows) { var e=(JSONObject)item; var counts=new ArrayList<String>(); for(Object c:e.optJSONArray("counts",new JSONArray())) counts.add(c.toString());
      out.add(new Rules.Event(e.getString("id"),e.getLong("at"),e.getString("crossing"),e.getString("text"),e.optBoolean("red"),counts)); }
    return out;
  }
  static Rules.TravelState travel(JSONArray rows) {
    var out=new ArrayList<Rules.Journey>();
    for(Object item:rows) { var e=(JSONObject)item;
      out.add(new Rules.Journey(e.getString("id"),e.getString("from"),e.isNull("to")?null:e.getString("to"),e.getInt("direction"),e.getLong("at"),e.getBoolean("passed"),e.getBoolean("released"),e.getBoolean("reached"),e.isNull("successorId")?null:e.getString("successorId"),e.getLong("lastAt"),e.optBoolean("selfTriggered"),e.optBoolean("stalled"))); }
    return new Rules.TravelState(out);
  }
  static List<Rules.Event> between(List<Rules.Event> rows,long from,long to) {return rows.stream().filter(e->e.at()>=from&&e.at()<to).toList();}
  static JSONObject analyze(JSONObject in) throws Exception {
    long now=in.getLong("now");
    var day=Instant.ofEpochMilli(now).atZone(WebSource.ZONE).toLocalDate();
    long dayStart=day.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
    long boundary=day.minusDays(1).atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
    long floor=Rules.historyCutoff(now);
    var route=Rules.accountRoute(in.optString("user"),new Rules.Route(in.getJSONArray("route").toList().stream().map(Object::toString).toList()));
    var raw=events(in.getJSONArray("events"));
    var previous=in.optJSONObject("previous",new JSONObject());
    long last=previous.optLong("lastSuccess");
    boolean historical=in.optBoolean("historical");
    boolean reset=!historical && (in.optBoolean("forceReset") || Rules.needsNewBaseline(last,now,in.optBoolean("restarted")));
    boolean newDay=last>0&&Instant.ofEpochMilli(last).atZone(WebSource.ZONE).toLocalDate().isBefore(day);
    long baseline=historical ? dayStart : Rules.faultBaseline(reset?(newDay?dayStart:now):previous.optLong("baseline"),now);
    var originals=raw.stream().filter(e->!Rules.isGenerated(e)).toList();
    var pending=new HashMap<String,Long>();
    var p=previous.optJSONObject("pending",new JSONObject()); for(String k:p.keySet()) pending.put(k,p.getLong(k));
    var fresh=new HashSet<>(in.optJSONArray("added",new JSONArray()).toList());
    var candidates=Rules.suspectedSelfTriggers(route,between(originals,Math.max(floor,boundary),now+1),Math.max(floor,boundary));
    var ids=candidates.stream().map(Rules.Event::id).collect(Collectors.toSet());
    var discard=raw.stream().filter(e->e.at()>=Math.max(floor,boundary)&&e.at()<=now).filter(Rules::isSelfTrigger).filter(e->!ids.contains(e.id().substring(5))).map(Rules.Event::id).toList();
    raw.removeIf(e->discard.contains(e.id()));
    var eligible=historical ? List.<Rules.Event>of() : candidates.stream().filter(e->last>0&&!reset&&(fresh.contains(e.id())||pending.containsKey(e.id()))).toList();
    if (last==0||reset) pending.clear();
    // Only a successful source read may confirm after ten seconds.
    var confirmed=in.optBoolean("maintenance") ? List.<Rules.Event>of() : Rules.confirmedSelfTriggers(eligible,pending,now,0,10000);
    var existing=raw.stream().map(Rules.Event::id).collect(Collectors.toSet());
    for(var e:confirmed) if(existing.add(e.id())) raw.add(e);
    var daily=Rules.dailyRecords(route,between(raw,dayStart,now+1));
    var historyGuesses=List.<Rules.Event>of();
    if(historical) {
      historyGuesses=Rules.historicalSelfTriggers(route,originals,Math.max(floor,boundary),dayStart,now+1,30);
      for(var e:historyGuesses) if(existing.add(e.id())) raw.add(e);
      daily=Rules.dailyRecords(route,between(raw,dayStart,now+1));
    }
    var faults=Rules.activeFaults(daily.stream().filter(e->e.at()>=baseline).toList());
    var cp=travel(previous.optJSONArray("checkpoint",new JSONArray()));
    long cpAt=previous.optLong("checkpointAt",boundary);
    boolean same=previous.optJSONArray("route",new JSONArray()).toString().equals(new JSONArray(route.points()).toString());
    long earliest=in.optLong("earliest",now);
    if(historical) cp=Rules.TravelState.empty();
    else if(!same||cpAt>boundary||earliest<cpAt||!discard.isEmpty()) {
      var expired=same?cp.journeys().stream().filter(j->j.at()<floor).toList():List.<Rules.Journey>of();
      cp=Rules.travelState(route,new Rules.TravelState(expired),between(raw,floor,boundary)); cpAt=boundary;
    } else if(cpAt<boundary) { cp=Rules.travelState(route,cp,between(raw,cpAt,boundary)); cpAt=boundary; }
    Rules.TravelState current=historical ? Rules.travelState(route,Rules.TravelState.empty(),raw) : Rules.travelState(route,cp,between(raw,boundary,now+1));
    var pointInfo=new ArrayList<Map<String,Object>>();
    for(String point:route.points()) pointInfo.add(Map.of("key",point,"station",Rules.isStation(point),"label",Rules.isStation(point)?"Ga "+Rules.stationLabel(point):point));
    var devices=new HashMap<String,Map<String,List<Rules.Event>>>();
    for(var e:faults) devices.computeIfAbsent(Rules.km(e.crossing()),k->new HashMap<>()).computeIfAbsent(Rules.deviceGroup(e),k->new ArrayList<>()).add(e);
    var groups=Rules.crossingHistory(daily,in.optString("crossing"));
    var announcements=new HashMap<String,String>();for(var e:faults)announcements.put(e.id(),AlertOptions.announcement(e));
    var out=new JSONObject();
    out.put("webRulesVersion",2).put("route",json(route.points())).put("points",json(pointInfo)).put("faults",json(faults)).put("incidents",json(Rules.incidentHistory(daily)))
      .put("segments",json(Rules.trackSegments(route,current))).put("occupancies",json(Rules.occupancies(route,raw)))
      .put("trains",json(between(raw,dayStart,now+1).stream().filter(e->Rules.direction(e.text())!=0).sorted(Comparator.comparingLong(Rules.Event::at).reversed()).toList()))
      .put("devices",json(devices)).put("groups",json(groups)).put("axles",json(Rules.axleErrors(route,between(raw,dayStart,now+1))))
      .put("checkpoint",json(cp.journeys())).put("checkpointAt",boundary).put("travel",json(current.journeys()))
      .put("pending",json(pending)).put("derived",json(historical?historyGuesses:confirmed)).put("discard",json(discard)).put("floor",floor).put("baseline",baseline)
      .put("records",json(daily)).put("announcements",json(announcements));
    return out;
  }
  public static void main(String[] args) throws Exception {
    // Requests and credentials use stdin, never command arguments or logs.
    JSONObject out;
    try {
      var in=new JSONObject(new String(System.in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
      if(in.getString("op").equals("fetch")) {
        var source=new WebSource(in.getString("url"),in.getString("user"),in.getString("password"));
        var known=new HashSet<>(in.optJSONArray("known",new JSONArray()).toList().stream().map(Object::toString).toList());
        var data=source.readDelta(LocalDate.parse(in.getString("day")),known);
        out=new JSONObject().put("events",json(data.events())).put("route",json(Rules.accountRoute(in.getString("user"),data.route()).points())).put("connected",data.connected());
      } else out=analyze(in);
      out.put("ok",true);
    } catch(Exception e) {
      // Do not echo website responses/credentials in an unauthenticated error.
      String type=e instanceof WebSource.AuthenticationException?"auth":e instanceof IOException?"network":"data";
      out=new JSONObject().put("ok",false).put("type",type).put("error",switch(type){case "auth"->"Không đăng nhập được website. Kiểm tra tài khoản.";case "network"->"Mất kết nối tới website.";default->"Chưa đọc được báo cáo đầy đủ. Dữ liệu cũ được giữ.";});
    }
    System.out.println(out);
  }
}
