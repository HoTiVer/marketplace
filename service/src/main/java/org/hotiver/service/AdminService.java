package org.hotiver.service;

import jakarta.transaction.Transactional;
import org.hotiver.common.RoleType;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final ChatService chatService;
    private final SellerRegisterRepo sellerRegisterRepo;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final RoleRepo roleRepo;
    private final EmailService emailService;

    public AdminService(ChatService chatService, SellerRegisterRepo sellerRegisterRepo,
                        UserRepo userRepo, SellerRepo sellerRepo,
                        RoleRepo roleRepo, EmailService emailService) {
        this.chatService = chatService;
        this.sellerRegisterRepo = sellerRegisterRepo;
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.roleRepo = roleRepo;
        this.emailService = emailService;
    }

    public List<SellerRegister> getSellerRegisterRequests() {
        return sellerRegisterRepo.findAll();
    }

    @Transactional
    public ResponseEntity<?> acceptSellerRegisterRequest(Long id) {
        var sellerRegister = sellerRegisterRepo.findById(id);
        if (sellerRegister.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Long userId = sellerRegister.get().getUserId();
        User user = userRepo.findById(userId).get();
        var roles = user.getRoles();
        roles.add(roleRepo.findById(3L).get());
        String sellerUsername = sellerRegister.get().getRequestedNickname();

        if (sellerRepo.existsByNickname(sellerUsername)){
            return ResponseEntity.badRequest().build();
        }

        user.setDisplayName(sellerRegister.get().getDisplayName());
        Seller seller = Seller.builder()
                .user(user)
                .rating(0.0)
                .nickname(sellerUsername)
                .profileDescription(sellerRegister.get().getProfileDescription())
                .build();

        sellerRepo.save(seller);

        chatService.sendMessage(0L, seller.getId(), "You are a seller now.");
        emailService.send(user.getEmail(), "Seller request", "You are a seller now.");

        sellerRegisterRepo.delete(sellerRegister.get());

        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> declineSellerRegisterRequest(Long id) {

        Optional<SellerRegister> sellerRegister = sellerRegisterRepo.findById(id);

        if (sellerRegister.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        chatService.sendMessage(0L, sellerRegister.get().getId(),
                "You are not allowed to be a seller.");

        sellerRegisterRepo.delete(sellerRegister.get());
        return ResponseEntity.ok().build();
    }
}
