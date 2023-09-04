package com.stock.service;

import com.stock.exception.DividendException;
import com.stock.model.Auth;
import com.stock.persist.MemberRepository;
import com.stock.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.stock.type.ErrorCode.*;


@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws RuntimeException {
        return this.memberRepository.findByUsername(username).orElseThrow(() ->
                new RuntimeException());
    }

    public MemberEntity register(Auth.SignUp member) {
        // 아이디가 존재하는 경우 exception 발생
        boolean exists = this.memberRepository.existsByUsername(member.getUsername()); // not implemented yet
        if (exists) {
            throw new DividendException(USER_ALREADY_EXIST, USER_ALREADY_EXIST.getStatus(), USER_ALREADY_EXIST.getDescription());//RuntimeException("이미 사용중인 아이디 입니다");
        }

        // ID 생성 가능한 경우, 멤버 테이블에 저장
        // 비밀번호는 암호화 되어서 저장되어야함
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        return this.memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        // id 로 멤버 조회
        var user = this.memberRepository.findByUsername(member.getUsername()); //.orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));
        if (user.isEmpty()) {
            throw new DividendException(INVALID_USER, INVALID_USER.getStatus(), INVALID_USER.getDescription());
        }

        if (!this.passwordEncoder.matches(member.getPassword(), user.get().getPassword())) { //패스워드 일치 여부 확인
            throw new DividendException(UNMATCHED_USER_ID_PW, UNMATCHED_USER_ID_PW.getStatus(), UNMATCHED_USER_ID_PW.getDescription());
        }                                                                                    //일치하지 않는 경우 400 status 코드와 적합한 에러 메시지 반환
        return user.get();                                                                   //일치하는 경우, 해당 멤버 엔티티 반환
    }
}
