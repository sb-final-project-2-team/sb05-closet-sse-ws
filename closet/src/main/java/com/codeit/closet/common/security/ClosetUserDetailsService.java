package com.codeit.closet.common.security;

import com.codeit.closet.common.entity.User;
import com.codeit.closet.common.security.user.UserMapper;
import com.codeit.closet.common.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClosetUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(()
                -> new UsernameNotFoundException("유저를 찾을 수 없습니다." + email));
        return new ClosetUserDetails(userMapper.toUserDTO(user), user.getPassword());
    }
}
