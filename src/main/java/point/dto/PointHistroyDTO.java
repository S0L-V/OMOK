package point.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class PointHistroyDTO {
	
	private String id;
	private String gameId;
	private String userId;
	private Timestamp createdAt;
}
