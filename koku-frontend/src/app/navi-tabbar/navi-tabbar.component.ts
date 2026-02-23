import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'koku-navi-tabbar',
  imports: [RouterLinkActive, RouterLink, RouterOutlet],
  templateUrl: './navi-tabbar.component.html',
})
export class NaviTabbarComponent {
  activatedRoute = inject(ActivatedRoute);
}
