package epu.aeshop.domain.view;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfo {
    private String email;
    private String fullName;
    private String joinedDate;
    private String avatarUrl;
}
