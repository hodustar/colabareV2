package cot.colabare.meetingboard.domain;

import org.springframework.web.util.UriComponentsBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class MeettingBoardCriteria {

	private int pageNum;
	private int amount;

	private String type;
	private String keyword;

	public MeettingBoardCriteria() {
		this(1, 10);
	}

	public MeettingBoardCriteria(int pageNum, int amount) {
		this.pageNum = pageNum;
		this.amount = amount;
	}

	public String[] getTypeArr() {

		return type == null ? new String[] {} : type.split("");
	}

	public String getListLink() {

		// UriComponentsBuilder는 브라우저에서 GET방식 등의 파라미터 전송에 사용되는 문자열(query string)을 손쉽게 처리할 수 있는 클래스
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("")
				.queryParam("pageNum", this.pageNum)
				.queryParam("amount", this.getAmount())
				.queryParam("type", this.getType())
				.queryParam("keyword", this.getKeyword());

		return builder.toUriString();

	}
}
