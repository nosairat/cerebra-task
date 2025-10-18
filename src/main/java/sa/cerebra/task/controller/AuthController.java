package sa.cerebra.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sa.cerebra.task.dto.request.LoginRequest;
import sa.cerebra.task.dto.request.ValidateOtpRequest;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        authService.login(request.getPhone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<?> validateOtp(@Valid @RequestBody ValidateOtpRequest request){
        TokenResponse response =  authService.validate(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(response);
    }


}
