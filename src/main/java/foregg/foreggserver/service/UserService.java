package foregg.foreggserver.service;

import foregg.foreggserver.converter.JsonConverter;
import foregg.foreggserver.converter.SurgeryConverter;
import foregg.foreggserver.converter.UserConverter;
import foregg.foreggserver.domain.Surgery;
import foregg.foreggserver.domain.User;
import foregg.foreggserver.dto.UserJoinRequestDTO;
import foregg.foreggserver.dto.kakaoDTO.KakaoUserInfoResponse;
import foregg.foreggserver.jwt.JwtTokenProvider;
import foregg.foreggserver.repository.SurgeryRepository;
import foregg.foreggserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SurgeryRepository surgeryRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoRequestService kakaoRequestService;

    // DB에 유저 정보 저장하고, JWT 토큰 만들어서 보내주기
    public String join(String token, UserJoinRequestDTO dto) {
        KakaoUserInfoResponse userInfo = kakaoRequestService.getUserInfo(token);
        Long userId = userInfo.getId();

        String jwtToken = jwtTokenProvider.createToken(userId.toString());

        HashMap<Long, String> map = new HashMap<>();
        map.put(userId, jwtToken);

        String keyCode = jwtTokenProvider.getUserPk(jwtToken);

        Surgery surgery = surgeryRepository.save(SurgeryConverter.toSurgery(dto));
        userRepository.save(UserConverter.toUser(userInfo, keyCode, surgery));

        return JsonConverter.convertToJson(map);
    }
}
