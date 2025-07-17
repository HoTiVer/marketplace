package org.hotiver.service;

import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.repo.SellerRegisterRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final SellerRegisterRepo sellerRegisterRepo;

    public AdminService(SellerRegisterRepo sellerRegisterRepo) {
        this.sellerRegisterRepo = sellerRegisterRepo;
    }

    public List<SellerRegister> getSellerRegisterRequests() {
        return sellerRegisterRepo.findAll();
    }

    public void acceptSellerRegisterRequest(String id) {

    }

    public void declineSellerRegisterRequest(String id) {
    }
}
