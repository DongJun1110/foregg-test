package foregg.foreggserver.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogoutWithdrawalResponseDTO {
    private String content;
}
