import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {NgForm} from "@angular/forms";
import {AuthService} from "../auth.service";

@Component({
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements AfterViewInit {

  @ViewChild('loginForm', {read: NgForm, static: false}) loginForm: NgForm | undefined;
  @ViewChild('username') loginField: ElementRef<HTMLInputElement> | undefined;
  loading = false;

  constructor(private readonly router: Router,
              private readonly authService: AuthService,
              private readonly cdr: ChangeDetectorRef) {
  }

  login(username: HTMLInputElement,
        password: HTMLInputElement) {
    if (!this.loading && this.loginForm && this.loginForm.valid) {
      this.loading = true;
      this.authService.createSession(String(username.value), String(password.value)).subscribe(() => {
        this.loading = false;
        this.router.navigate(['/welcome'], {
          replaceUrl: true
        });
      }, () => {
        this.loading = false;
      });
    }
  }

  ngAfterViewInit(): void {
    if (this.loginField) {
      this.loginField.nativeElement.focus();
    }
    this.cdr.detectChanges()
  }

}
