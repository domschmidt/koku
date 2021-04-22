import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";

@Injectable()
export class NaviService {

  public openState: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() {}

  public close(): void {
    this.openState.next(false);
  }
  public open(): void {
    this.openState.next(true);
  }

}
