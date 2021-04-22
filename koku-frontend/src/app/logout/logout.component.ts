import {Component} from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../auth.service";

@Component({
  selector: 'logout',
  templateUrl: './logout.component.html',
})
export class LogoutComponent {

  loading = false;

  constructor(private readonly authService: AuthService,
              private readonly router: Router) {
    this.loading = true;
    authService.destroySession().subscribe(() => {
      router.navigate(['/login']);
      this.loading = false;
    }, () => {
      router.navigate(['/login']);
      this.loading = false;
    });
  }

}
