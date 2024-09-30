package art.heredium.domain.docent.entity;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.docent.model.dto.request.PostAdminDocentRequest;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Table(name = "docent_info")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"docent"})
//도슨트 정보
public class DocentInfo implements Serializable {
    private static final long serialVersionUID = 2293746755073113633L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docent_id", nullable = false)
    private Docent docent;

    @Comment("썸네일")
    @Type(type = "json")
    @Column(name = "thumbnail", columnDefinition = "json")
    private Storage thumbnail;

    @Comment("제목")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Comment("작가명")
    @Column(name = "writer", nullable = false, length = 30)
    private String writer;

    @Comment("작품위치")
    @Column(name = "position", nullable = false, length = 100)
    private String position;

    @Comment("소개")
    @Column(name = "intro", nullable = false, columnDefinition = "TEXT")
    private String intro;

    @Comment("음성파일")
    @Type(type = "json")
    @Column(name = "auido", columnDefinition = "json")
    private Storage audio;

    @Comment("지도 이미지")
    @Type(type = "json")
    @Column(name = "map", columnDefinition = "json")
    private Storage map;

    @Comment("순서")
    @Column(name = "orders", nullable = false)
    private Long order;

    public DocentInfo(PostAdminDocentRequest.Info dto, Docent docent, long order) {
        this.docent = docent;
        this.thumbnail = dto.getThumbnail();
        this.title = dto.getTitle();
        this.writer = dto.getWriter();
        this.position = dto.getPosition();
        this.intro = dto.getIntro();
        this.audio = dto.getAudio();
        this.map = dto.getMap();
        this.order = order;
    }

    public void update(PostAdminDocentRequest.Info dto, long order) {
        this.thumbnail = dto.getThumbnail();
        this.title = dto.getTitle();
        this.writer = dto.getWriter();
        this.position = dto.getPosition();
        this.intro = dto.getIntro();
        this.audio = dto.getAudio();
        this.map = dto.getMap();
        this.order = order;
    }
}
