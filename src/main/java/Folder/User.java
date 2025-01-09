package Folder;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private Long chatId;
    private String userName;
    private String firstName;
    private Status status;
}
