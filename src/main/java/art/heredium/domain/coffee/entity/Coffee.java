package art.heredium.domain.coffee.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.*;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.coffee.model.dto.request.PostAdminCoffeeRequest;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.model.ProjectInfo;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.domain.ticket.model.TicketInviteCreateInfo;
import art.heredium.domain.ticket.model.TicketInviteInfo;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketInviteRequest;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.ncloud.bean.CloudStorage;

@Entity
@Getter
@Table(name = "coffee")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
// 커피
public class Coffee extends BaseEntity implements TicketInviteInfo, ProjectInfo, Serializable {
  private static final long serialVersionUID = -2461558575926699721L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("썸네일")
  @Type(type = "json")
  @Column(name = "thumbnail", columnDefinition = "json")
  private Storage thumbnail;

  @Comment("상세 이미지")
  @Type(type = "json")
  @Column(name = "detail_image", columnDefinition = "json")
  private Storage detailImage;

  @Comment("제목")
  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Comment("부제목")
  @Column(name = "subtitle", nullable = false, length = 100)
  private String subtitle;

  @Comment("hall 구분")
  @Type(type = "json")
  @Column(name = "halls", columnDefinition = "json", nullable = false)
  private List<HallType> halls = new ArrayList<>();

  @Comment("활성화 여부")
  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Comment("시작일")
  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Comment("종료일")
  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Comment("예약시작 가능일")
  @Column(name = "booking_date", nullable = false)
  private LocalDateTime bookingDate;

  @Comment("시간")
  @Column(name = "hours", nullable = false, columnDefinition = "TEXT")
  private String hour;

  @Comment("내용")
  @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
  private String contents;

  @Comment("비고")
  @Column(name = "note", nullable = false, columnDefinition = "TEXT")
  private String note;

  @Comment("버튼 표기 여부")
  @ColumnDefault("0")
  @Column(name = "is_use_button", nullable = false)
  private Boolean isUseButton;

  @Comment("버튼 명")
  @Column(name = "button_title", nullable = false)
  private String buttonTitle;

  @Comment("버튼 링크")
  @Column(name = "button_link", nullable = false, length = 2048)
  private String buttonLink;

  @Comment("새창 열기 여부")
  @ColumnDefault("0")
  @Column(name = "is_new_tab", nullable = false)
  private Boolean isNewTab;

  @OneToMany(mappedBy = "coffee", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("startDate ASC")
  private List<CoffeeRound> rounds = new ArrayList<>();

  @OneToMany(mappedBy = "coffee", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CoffeePrice> prices = new ArrayList<>();

  @OneToMany(mappedBy = "coffee", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  private List<CoffeeWriter> writers = new ArrayList<>();

  public Coffee(PostAdminCoffeeRequest dto) {
    this.thumbnail = dto.getThumbnail();
    this.detailImage = dto.getDetailImage();
    this.title = dto.getTitle();
    this.subtitle = dto.getSubtitle();
    this.halls = dto.getHalls();
    this.isEnabled = dto.getIsEnabled();
    this.startDate = dto.getStartDate();
    this.endDate = dto.getEndDate();
    this.bookingDate = dto.getBookingDate();
    this.hour = dto.getHour();
    this.contents = dto.getContents();
    this.note = "";
    this.isUseButton = dto.getIsUseButton();
    this.buttonTitle = dto.getButtonTitle();
    this.buttonLink = dto.getButtonLink();
    this.isNewTab = dto.getIsNewTab();
    this.rounds =
        dto.getRounds().stream()
            .map(round -> new CoffeeRound(round, this))
            .collect(Collectors.toList());
    this.prices =
        dto.getPrices().stream()
            .map(price -> new CoffeePrice(price, this))
            .collect(Collectors.toList());
    this.writers =
        IntStream.range(0, dto.getWriters().size())
            .mapToObj(index -> new CoffeeWriter(dto.getWriters().get(index), this, index))
            .collect(Collectors.toList());
  }

  public void update(PostAdminCoffeeRequest dto) {
    dto.validate(this);
    this.thumbnail = dto.getThumbnail();
    this.detailImage = dto.getDetailImage();
    this.title = dto.getTitle();
    this.subtitle = dto.getSubtitle();
    this.halls = dto.getHalls();
    this.isEnabled = dto.getIsEnabled();
    this.startDate = dto.getStartDate();
    this.endDate = dto.getEndDate();
    this.bookingDate = dto.getBookingDate();
    this.hour = dto.getHour();
    this.contents = dto.getContents();
    this.isUseButton = dto.getIsUseButton();
    this.buttonTitle = dto.getButtonTitle();
    this.buttonLink = dto.getButtonLink();
    this.isNewTab = dto.getIsNewTab();
    this.updateLastModifiedDate();
    this.updateLastModifiedName();

    this.getRounds()
        .removeIf(
            round ->
                dto.getRounds().stream()
                    .noneMatch(
                        dtoRound ->
                            dtoRound.getId() != null
                                && dtoRound.getId() == round.getId().intValue()));
    dto.getRounds()
        .forEach(
            dtoRound -> {
              CoffeeRound up =
                  this.getRounds().stream()
                      .filter(
                          round ->
                              dtoRound.getId() != null
                                  && round.getId().intValue() == dtoRound.getId())
                      .findAny()
                      .orElse(null);
              if (up == null) {
                this.addRound(new CoffeeRound(dtoRound, this));
              } else {
                up.update(dtoRound);
              }
            });

    this.getPrices()
        .removeIf(
            price ->
                dto.getPrices().stream()
                    .noneMatch(
                        dtoPrice ->
                            dtoPrice.getId() != null
                                && dtoPrice.getId() == price.getId().intValue()));
    dto.getPrices()
        .forEach(
            dtoPrice -> {
              CoffeePrice up =
                  this.getPrices().stream()
                      .filter(
                          price ->
                              dtoPrice.getId() != null
                                  && price.getId().intValue() == dtoPrice.getId())
                      .findAny()
                      .orElse(null);
              if (up == null) {
                this.addPrice(new CoffeePrice(dtoPrice, this));
              } else {
                up.update(dtoPrice);
              }
            });

    this.getWriters()
        .removeIf(
            writer ->
                dto.getWriters().stream()
                    .noneMatch(
                        dtoWriter ->
                            dtoWriter.getId() != null
                                && dtoWriter.getId() == writer.getId().intValue()));
    IntStream.range(0, dto.getWriters().size())
        .forEach(
            index -> {
              PostAdminCoffeeRequest.Writer dtoWriter = dto.getWriters().get(index);
              CoffeeWriter up =
                  this.getWriters().stream()
                      .filter(
                          writer ->
                              dtoWriter.getId() != null
                                  && writer.getId().intValue() == dtoWriter.getId())
                      .findAny()
                      .orElse(null);
              if (up == null) {
                this.addWriter(new CoffeeWriter(dtoWriter, this, index));
              } else {
                up.update(dtoWriter, index);
              }
            });
  }

  public void addRound(CoffeeRound entity) {
    this.rounds.add(entity);
  }

  public void addPrice(CoffeePrice entity) {
    this.prices.add(entity);
  }

  public void addWriter(CoffeeWriter entity) {
    this.writers.add(entity);
  }

  public List<String> getRemoveFile(PostAdminCoffeeRequest dto) {
    List<Storage> entityStorages = new ArrayList<>();
    entityStorages.add(this.getThumbnail());
    entityStorages.add(this.getDetailImage());
    this.getWriters()
        .forEach(
            writer ->
                writer
                    .getInfos()
                    .forEach(writerInfo -> entityStorages.add(writerInfo.getThumbnail())));
    List<Storage> dtoStorages = new ArrayList<>();
    dtoStorages.add(dto.getThumbnail());
    dtoStorages.add(dto.getDetailImage());
    dto.getWriters()
        .forEach(
            writer ->
                writer
                    .getInfos()
                    .forEach(writerInfo -> dtoStorages.add(writerInfo.getThumbnail())));
    return entityStorages.stream()
        .filter(
            entityStorage ->
                entityStorage != null
                    && dtoStorages.stream()
                        .noneMatch(
                            dtoStorage ->
                                dtoStorage != null
                                    && dtoStorage
                                        .getSavedFileName()
                                        .equals(entityStorage.getSavedFileName())))
        .flatMap(x -> x.getAllFileName().stream())
        .collect(Collectors.toList());
  }

  public void applyTempContents(CloudStorage cloudStorage) {
    String path = getFileFolderPath();
    List<String> tempFiles = Constants.getEditorTempFile(this.contents, path);
    updateContent(Constants.replaceTempFile(this.contents, tempFiles, path));
    tempFiles.forEach(x -> cloudStorage.move(x, x.replace(FilePathType.EDITOR.getPath(), path)));
  }

  public void applyTempFile(CloudStorage cloudStorage) {
    Constants.moveFileFromTemp(cloudStorage, this.getThumbnail(), getFileFolderPath());
    Constants.moveFileFromTemp(cloudStorage, this.getDetailImage(), getFileFolderPath());
    this.getWriters()
        .forEach(
            writer -> {
              writer
                  .getInfos()
                  .forEach(
                      info -> {
                        Constants.moveFileFromTemp(
                            cloudStorage, info.getThumbnail(), getFileFolderPath());
                      });
            });
  }

  public LocalDateTime getBookingStartDate() {
    LocalDateTime now = Constants.getNow();
    if (now.isBefore(this.getBookingDate())) {
      return this.getStartDate();
    } else if (now.isBefore(this.getStartDate())) {
      return this.getStartDate();
    } else {
      return now;
    }
  }

  public LocalDateTime getBookingEndDate() {
    LocalDateTime now = Constants.getNow();
    LocalDateTime init;
    if (now.isBefore(this.getBookingDate())) {
      init = this.getStartDate();
    } else if (now.isBefore(this.getStartDate())) {
      init = this.getStartDate().plusDays(Constants.BOOKING_DATE).with(LocalTime.MAX);
    } else {
      init = now.plusDays(Constants.BOOKING_DATE).with(LocalTime.MAX);
    }
    if (init.isAfter(this.getEndDate())) {
      init = this.getEndDate();
    }
    return init;
  }

  public Log createInsertLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.COFFEE, LogAction.INSERT);
  }

  public Log createUpdateLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.COFFEE, LogAction.UPDATE);
  }

  public Log createDeleteLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.COFFEE, LogAction.DELETE);
  }

  public String getFileFolderPath() {
    return FilePathType.COFFEE.getPath() + "/" + this.id;
  }

  public void updateNote(String note) {
    this.note = note;
  }

  public void updateContent(String contents) {
    this.contents = contents;
  }

  public ProjectStateType getState() {
    return ProjectStateType.getState(this.getStartDate(), this.getEndDate(), this.getBookingDate());
  }

  @Override
  public TicketInviteCreateInfo getTicketCreateInfo(PostAdminTicketInviteRequest dto) {
    TicketInviteCreateInfo info = new TicketInviteCreateInfo();
    info.setKind(TicketKindType.COFFEE);
    info.setId(this.getId());
    info.setTitle(this.getTitle());
    info.setStartDate(this.getStartDate());
    info.setEndDate(this.getEndDate());
    info.setNumber(dto.getNumber());
    return info;
  }
}
