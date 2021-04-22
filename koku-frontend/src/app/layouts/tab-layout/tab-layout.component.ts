import {Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {NaviService} from "../../navi/navi.service";

@Component({
  selector: 'tab-layout',
  templateUrl: './tab-layout.component.html',
  styleUrls: ['./tab-layout.component.scss']
})
export class TabLayoutComponent {

  constructor(public readonly activatedRoute: ActivatedRoute,
              public readonly naviService: NaviService) {
  }

}
