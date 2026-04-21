package org.hotiver.service.user;

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

@Service
public class SellerRegisterService {

    private final ChatService chatService;
    private final SellerRegisterRepo sellerRegisterRepo;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final RoleRepo roleRepo;
    private final EmailService emailService;

    public SellerRegisterService(ChatService chatService, SellerRegisterRepo sellerRegisterRepo,
                                 UserRepo userRepo, SellerRepo sellerRepo, RoleRepo roleRepo, EmailService emailService) {
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
        User user = userRepo.findById(sellerRegister.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        validateNicknameExisting(sellerRegister.getRequestedNickname());

        updateUser(user, sellerRegister);
        Seller seller = createSeller(user, sellerRegister);

        sellerRepo.save(seller);

        chatService.sendMessage(0L, seller.getId(), "You are a seller now.");
        emailService.sendAsync(user.getEmail(), "Seller request", "You are a seller now.");

        sellerRegister.setStatus(SellerRegisterRequestStatus.ACCEPTED);
        sellerRegisterRepo.save(sellerRegister);
    }

    private void validateNicknameExisting(String sellerNickname) {
        if (sellerRepo.existsByNickname(sellerNickname)){
            throw new EntityAlreadyExistsException("Seller already exists");
        }
    }

    private void updateUser(User user, SellerRegister sellerRegister) {
        user.getRoles().add(roleRepo.findById(3L)
                .orElseThrow(() -> new EntityNotFoundException("Role not found")));

        user.setDisplayName(sellerRegister.getDisplayName());
    }

    private Seller createSeller(User user, SellerRegister sellerRegister) {
        return Seller.builder()
                .user(user)
                .rating(BigDecimal.valueOf(0.0))
                .nickname(sellerRegister.getRequestedNickname())
                .profileDescription(sellerRegister.getProfileDescription())
                .build();
    }

    @Transactional
    public void declineSellerRegisterRequest(Long id) {
        SellerRegister sellerRegister = sellerRegisterRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SellerRegister not found"));

        chatService.sendMessage(0L, sellerRegister.getUserId(),
                "You are not allowed to be a seller.");

        User user = userRepo.findById(sellerRegister.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        emailService.sendAsync(user.getEmail(), "Seller request",
                "You are not allowed to be a seller.");

        sellerRegister.setStatus(SellerRegisterRequestStatus.REJECTED);
        sellerRegisterRepo.save(sellerRegister);
    }
}
