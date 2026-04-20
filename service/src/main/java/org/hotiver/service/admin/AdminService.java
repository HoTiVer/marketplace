package org.hotiver.service.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.chat.ChatService;
import org.hotiver.service.email.EmailService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public List<SellerRegisterResponse> getSellerRegisterRequests() {
        return sellerRegisterRepo.findByStatus(SellerRegisterRequestStatus.ACTIVE);
    }

    @Transactional
    public void acceptSellerRegisterRequest(Long id) {
        SellerRegister sellerRegister = sellerRegisterRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SellerRegister not found"));

        Long userId = sellerRegister.getUserId();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var roles = user.getRoles();
        roles.add(roleRepo.findById(3L)
                .orElseThrow(() -> new EntityNotFoundException("Role not found")));

        String sellerUsername = sellerRegister.getRequestedNickname();

        if (sellerRepo.existsByNickname(sellerUsername)){
            throw new EntityAlreadyExistsException("Seller already exists");
        }

        user.setDisplayName(sellerRegister.getDisplayName());
        Seller seller = Seller.builder()
                .user(user)
                .rating(BigDecimal.valueOf(0.0))
                .nickname(sellerUsername)
                .profileDescription(sellerRegister.getProfileDescription())
                .build();

        sellerRepo.save(seller);

        chatService.sendMessage(0L, seller.getId(), "You are a seller now.");
        emailService.send(user.getEmail(), "Seller request", "You are a seller now.");

        //sellerRegisterRepo.delete(sellerRegister.get());
        sellerRegister.setStatus(SellerRegisterRequestStatus.ACCEPTED);
        sellerRegisterRepo.save(sellerRegister);
    }

    @Transactional
    public void declineSellerRegisterRequest(Long id) {
        SellerRegister sellerRegister = sellerRegisterRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SellerRegister not found"));

        chatService.sendMessage(0L, sellerRegister.getUserId(),
                "You are not allowed to be a seller.");

        User user = userRepo.findById(sellerRegister.getUserId()).get();

        emailService.send(user.getEmail(), "Seller request",
                "You are not allowed to be a seller.");

        //sellerRegisterRepo.delete(sellerRegister.get());
        sellerRegister.setStatus(SellerRegisterRequestStatus.REJECTED);
        sellerRegisterRepo.save(sellerRegister);
    }
}
