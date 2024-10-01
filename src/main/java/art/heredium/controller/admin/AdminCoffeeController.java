package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.CoffeePermission;
import art.heredium.domain.coffee.model.dto.request.GetAdminCoffeeRequest;
import art.heredium.domain.coffee.model.dto.request.PostAdminCoffeeRequest;
import art.heredium.domain.common.model.dto.response.Data;
import art.heredium.service.CoffeeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coffees")
public class AdminCoffeeController {

  private final CoffeeService coffeeService;

  @GetMapping
  @CoffeePermission
  public ResponseEntity list(@Valid GetAdminCoffeeRequest dto, Pageable pageable) {
    return ResponseEntity.ok(coffeeService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @CoffeePermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(coffeeService.detailByAdmin(id));
  }

  @PostMapping
  @CoffeePermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminCoffeeRequest dto) {
    return ResponseEntity.ok(coffeeService.insert(dto));
  }

  @PutMapping("/{id}")
  @CoffeePermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PostAdminCoffeeRequest dto) {
    return ResponseEntity.ok(coffeeService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @CoffeePermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(coffeeService.delete(id));
  }

  @GetMapping("/expose/title")
  @CoffeePermission
  public ResponseEntity exposeTitle() {
    return ResponseEntity.ok(coffeeService.exposeTitle());
  }

  @GetMapping("/{id}/rounds")
  @CoffeePermission
  public ResponseEntity detailRounds(@PathVariable Long id) {
    return ResponseEntity.ok(coffeeService.detailRounds(id));
  }

  @PutMapping("/{id}/note")
  @CoffeePermission
  public ResponseEntity updateNote(@PathVariable Long id, @RequestBody Data<String> data) {
    return ResponseEntity.ok(coffeeService.updateNote(id, data));
  }
}
