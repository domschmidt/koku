import {Component, Input} from "@angular/core";
import {NaviService} from "./navi.service";

export interface NaviNode {
  text: string;
  path: string;
  showChildrenWithinTabs?: boolean;
  children?: NaviNode[];
}

@Component({
  selector: 'navigation-entry',
  templateUrl: './navigation-entry.component.html',
  styleUrls: ['./navigation-entry.component.scss'],
})
export class NavigationEntryComponent {

  @Input() naviEntry: NaviNode | undefined;
  @Input() depth: number = 0;

  constructor(public readonly naviService: NaviService) {
  }
}
