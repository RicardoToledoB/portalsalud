package cl.dssm.soporteimagenes.controller;

import cl.dssm.soporteimagenes.dto.CreateUserDto;
import cl.dssm.soporteimagenes.dto.ResetPasswordDto;
import cl.dssm.soporteimagenes.dto.UpdateUserDto;
import cl.dssm.soporteimagenes.dto.UserDto;
import cl.dssm.soporteimagenes.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> list() {
        return userService.list();
    }

    @GetMapping("/responsables")
    public List<UserDto> listAssignableReferents() {
        return userService.listAssignableReferents();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @PatchMapping("/{id}/reset-password")
    public UserDto resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDto dto) {
        return userService.resetPassword(id, dto);
    }

    @PatchMapping("/{id}/enable")
    public UserDto enable(@PathVariable Long id) {
        return userService.setActive(id, true);
    }

    @PatchMapping("/{id}/disable")
    public UserDto disable(@PathVariable Long id) {
        return userService.setActive(id, false);
    }
}
