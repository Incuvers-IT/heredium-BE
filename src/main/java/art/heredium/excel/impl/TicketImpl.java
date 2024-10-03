package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.excel.interfaces.CreateBody;

public class TicketImpl implements CreateBody {

  private List<Ticket> list;

  public TicketImpl(List<Ticket> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList =
        Arrays.asList(
            "NO", "구분", "티켓구분", "제목", "회차", "구매 수", "금액", "계정", "이름", "연락처", "입장권아이디", "결제아이디",
            "생성일시", "상태");
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    DateTimeFormatter dtfs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    int cnt = 0;
    for (Ticket entity : list) {
      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getKind().getDesc()),
              createString(entity.getType().getDesc()),
              createString(entity.getTitle()),
              createString(
                  String.format(
                      "%s ~ %S",
                      entity.getStartDate().format(dtfm), entity.getEndDate().format(dtfm))),
              createString(entity.getNumber()),
              createString(entity.getPrice()),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()),
              createString(entity.getUuid()),
              createString(entity.getPgId()),
              createString(entity.getCreatedDate().format(dtfs)),
              createString(entity.getState().getDesc()));
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}