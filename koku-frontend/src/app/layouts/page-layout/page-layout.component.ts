import {Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {NaviService} from "../../navi/navi.service";

@Component({
  selector: 'page-layout',
  templateUrl: './page-layout.component.html',
  styleUrls: ['./page-layout.component.scss']
})
export class PageLayoutComponent {

  constructor(public readonly activatedRoute: ActivatedRoute,
              public readonly naviService: NaviService) {
  }

}
