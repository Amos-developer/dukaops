package com.dukaops.backend.contact.dto; 
import jakarta.validation.constraints.*; 
public record CustomerRequest(@NotBlank @Size(max=150) String name,@Size(max=40) String phone,@Email @Size(max=254) String email,String address,String notes){}
