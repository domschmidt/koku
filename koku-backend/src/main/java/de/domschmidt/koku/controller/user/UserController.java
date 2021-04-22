package de.domschmidt.koku.controller.user;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.user.KokuUserDetailsDto;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.service.IUserService;
import de.domschmidt.koku.service.searchoptions.KokuUserSearchOptions;
import de.domschmidt.koku.transformer.KokuUserToKokuUserDetailsDtoTransformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController<KokuUser, KokuUserDetailsDto, KokuUserSearchOptions> {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final IUserService userService;

    @Autowired
    public UserController(final KokuUserToKokuUserDetailsDtoTransformer transformer,
                          final BCryptPasswordEncoder bCryptPasswordEncoder,
                          final IUserService userService) {
        super(userService, transformer);
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping
    public List<KokuUserDetailsDto> findAll(final KokuUserSearchOptions kokuUserSearchOptions) {
        return super.findAll(kokuUserSearchOptions);
    }

    @GetMapping(value = "/{id}")
    public KokuUserDetailsDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("id") Long id, @RequestBody KokuUserDetailsDto updatedDto) {
        final KokuUser kokuUser = this.userService.findById(id);
        kokuUser.setUsername(updatedDto.getUsername());
        kokuUser.getUserDetails().setFirstname(updatedDto.getFirstname());
        kokuUser.getUserDetails().setLastname(updatedDto.getLastname());
        kokuUser.getUserDetails().setAvatarBase64(updatedDto.getAvatarBase64());
        if (StringUtils.isNotEmpty(updatedDto.getPassword())) {
            kokuUser.setPassword(this.bCryptPasswordEncoder.encode(updatedDto.getPassword()));
        }
        this.userService.update(kokuUser);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void delete(@PathVariable("id") Long id) {
        final KokuUser kokuUser = this.userService.findById(id);
        kokuUser.setDeleted(true);
        this.userService.update(kokuUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuUserDetailsDto create(@RequestBody KokuUserDetailsDto newDto) {
        final KokuUser newKokuUser = this.transformer.transformToEntity(newDto);
        newKokuUser.setPassword(this.bCryptPasswordEncoder.encode(newDto.getPassword()));
        final KokuUser savedUser = this.userService.create(newKokuUser);
        return this.transformer.transformToDto(savedUser);
    }


}
