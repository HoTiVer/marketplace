package org.hotiver.service.user;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.repo.core.RoleRepo;
import org.hotiver.repo.core.SellerRegisterRepo;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.repo.projection.SellerRegisterProjectionRepo;
import org.hotiver.service.chat.ChatService;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.email.EmailService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class SellerRegisterService {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;
    private final SellerRegisterRepo sellerRegisterRepo;
    private final SellerRegisterProjectionRepo sellerRegisterProjectionRepo;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final RoleRepo roleRepo;
    private final EmailService emailService;

    public SellerRegisterService(ChatService chatService,
                                 CurrentUserService currentUserService,
                                 SellerRegisterRepo sellerRegisterRepo,
                                 UserRepo userRepo, SellerRepo sellerRepo,
                                 RoleRepo roleRepo, EmailService emailService,
                                 SellerRegisterProjectionRepo sellerRegisterProjectionRepo) {
        this.chatService = chatService;
        this.currentUserService = currentUserService;
        this.sellerRegisterRepo = sellerRegisterRepo;
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.roleRepo = roleRepo;
        this.emailService = emailService;
        this.sellerRegisterProjectionRepo = sellerRegisterProjectionRepo;
    }

    public List<SellerRegisterResponse> getSellerRegisterRequests() {
        return sellerRegisterProjectionRepo.findByStatus(SellerRegisterRequestStatus.ACTIVE);
    }

    public void sendSellerRegisterRequest(SellerRegisterDto sellerRegisterDto) {
        validateNickname(sellerRegisterDto.getRequestedNickname());

        isSellerWithNickNameExists(sellerRegisterDto.getRequestedNickname());

        SecurityUser user = currentUserService.getUserPrincipal();
        Long userId = user.getId();

        isYouAlreadySeller(userId);

        isAlreadySentRequest(userId);

        SellerRegister sellerRegister = SellerRegister.builder()
                .userId(userId)
                .requestedNickname(sellerRegisterDto.getRequestedNickname())
                .displayName(sellerRegisterDto.getDisplayName())
                .profileDescription(sellerRegisterDto.getDescription())
                .requestDate(Date.from(Instant.now()))
                .status(SellerRegisterRequestStatus.ACTIVE)
                .build();

        sellerRegisterRepo.save(sellerRegister);
    }

    private void validateNickname(String nickname) {
        if (!nickname.matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException(
                    "Nickname may contain only English letters and numbers"
            );
        }
    }

    private void isSellerWithNickNameExists(String nickname) {
        if (sellerRepo.existsByNickname(nickname)){
            throw new EntityAlreadyExistsException("Seller with nickname "
                    + nickname
                    + " already exists");
        }
    }

    private void isYouAlreadySeller(Long userId) {
        if (sellerRepo.existsById(userId)){
            throw new EntityAlreadyExistsException("You are already a seller");
        }
    }

    private void isAlreadySentRequest(Long userId) {
        Date today = Date.from(Instant.now());
        if (sellerRegisterRepo.existsByUserIdAndRequestDate(userId, today)) {
            throw new EntityAlreadyExistsException("You already send a request today");
        }

        if (sellerRegisterRepo.existsByUserIdAndStatus(userId, SellerRegisterRequestStatus.ACTIVE)) {
            throw new EntityAlreadyExistsException("You already send a request, wait for answer.");
        }
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
