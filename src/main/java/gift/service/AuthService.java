package gift.service;

import gift.controller.auth.LoginRequest;
import gift.controller.auth.Token;
import gift.domain.Member;
import gift.exception.FailedHashException;
import gift.exception.MemberNotExistsException;
import gift.exception.PasswordNotMatchedException;
import gift.repository.MemberRepository;
import gift.util.HashUtil;
import gift.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final KakaoConfig kakaoConfig;

    public AuthService(MemberRepository memberRepository, KakaoConfig kakaoConfig) {
        this.memberRepository = memberRepository;
        this.kakaoConfig = kakaoConfig;
    }

    public Token login(LoginRequest member) {
        Member m = memberRepository.findByEmail(member.email())
            .orElseThrow(MemberNotExistsException::new);
        String encryptedPassword;
        try {
            encryptedPassword = HashUtil.hashPassword(member.rawPassword());
        } catch (Exception e) {
            throw new FailedHashException();
        }
        if (!encryptedPassword.equals(m.getPassword())) {
            throw new PasswordNotMatchedException();
        }
        return new Token(JwtUtil.generateToken(m.getId(), m.getEmail()));
    }

    public String getAuthorizationUrl() {
        return String.format(
            "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
            kakaoConfig.getKakaoAppKey(), kakaoConfig.getRedirectUri());
    }

}