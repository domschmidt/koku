import {Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {NaviService} from "../../navi/navi.service";

@Component({
  selector: 'panel-layout',
  templateUrl: './panel-layout.component.html',
  styleUrls: ['./panel-layout.component.scss']
})
export class PanelLayoutComponent {

  constructor(public readonly activatedRoute: ActivatedRoute,
              public readonly naviService: NaviService) {
  }

}
