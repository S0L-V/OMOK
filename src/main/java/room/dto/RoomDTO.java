package room.dto;

import lombok.*;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @Builder
public class RoomDTO {
    private UUID id;
    private UUID hostUserId;
    private String roomName;
    private String roomPwd;
    private int isPublic;     
    private int playType;       
    private int totalUserCnt;
    private int currentUserCnt;
}
