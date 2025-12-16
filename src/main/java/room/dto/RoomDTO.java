package room.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @Builder
public class RoomDTO {
    private String id;
    private String hostUserId;
    private String roomName;
    private String roomPwd;
    private int isPublic;     
    private int playType;       
    private int totalUserCnt;
    private int currentUserCnt;
}
