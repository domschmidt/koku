import {booleanAttribute, Component, input} from '@angular/core';

@Component({
  selector: 'koku-avatar',
  imports: [],
  templateUrl: './avatar.component.html',
  styleUrl: './avatar.component.css'
})
export class AvatarComponent {

  value = input<string>()
  loading = input(false, {transform: booleanAttribute});

}
