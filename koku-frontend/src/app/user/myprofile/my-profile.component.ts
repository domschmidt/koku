import {Component} from '@angular/core';
import {MyUserDetailsService} from "../my-user-details.service";
import {NgForm} from "@angular/forms";

@Component({
  selector: 'myprofile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['./my-profile.component.scss']
})
export class MyProfileComponent {
  userDetails: KokuDto.KokuUserDetailsDto | undefined;
  loading = false;

  constructor(private readonly service: MyUserDetailsService) {
    this.service.getDetails().subscribe((userDetails) => {
      this.userDetails = {...userDetails};
    });
  }

  save(profileForm: NgForm, userDetails: KokuDto.KokuUserDetailsDto) {
    if (profileForm.valid) {
      this.loading = true;
      this.service.updateDetails(userDetails).subscribe(() => {
        this.loading = false;
      }, () => {
        this.loading = false;
      });
    }
  }
}
