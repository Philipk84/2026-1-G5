package com.example.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.user.UserAddDTO;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserRolePK;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
    public void addRoleToUser(long userId, long roleId) throws Exception {

        Optional<User> userO = userRepository.findById(userId);
        if (userO.isEmpty()) {
            throw new Exception("User not found");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new Exception("Role not found"));

        User user = userO.get();

        boolean alreadyAssigned = user.getRoles().stream()
                .anyMatch(ur -> ur.getRole().getId().equals(roleId));

        if (alreadyAssigned) {
            return;
        }

        UserRolePK pk = new UserRolePK();
        pk.setUserId(user.getId());
        pk.setRoleId(role.getId());

        UserRole userRole = new UserRole();
        userRole.setId(pk);
        userRole.setUser(user);
        userRole.setRole(role);

        user.getRoles().add(userRole);
        userRepository.save(user);
    }

    @Transactional
    public User createUser(UserAddDTO dto) {

        if (dto.getName() == null || dto.getUsername() == null || dto.getPassword() == null) {
            throw new RuntimeException("Validations fail");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        if (dto.getRoleIds() != null) {
            for (Long roleId : dto.getRoleIds()) {
                try {
                    addRoleToUser(savedUser.getId(), roleId);
                } catch (Exception e) {
                    throw new RuntimeException("Error assigning role: " + roleId, e);
                }
            }
        }

        return savedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        System.out.println(passwordEncoder.encode("123"));
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new UserDetailsCustomer(user);
    }
}