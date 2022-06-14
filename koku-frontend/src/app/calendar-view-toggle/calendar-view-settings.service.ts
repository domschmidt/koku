import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";

export type ViewIdentifier = 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay';

@Injectable()
export class CalendarViewSettingsService {
  private static readonly CALENDAR_VIEW_LOCAL_STORAGE_KEY = 'lastCalendarViewIdentifier';

  public activeViewIdentifier: BehaviorSubject<ViewIdentifier> = new BehaviorSubject<ViewIdentifier>(this.restoreViewIdentifier());

  private restoreViewIdentifier(): ViewIdentifier {
    let result: ViewIdentifier = 'timeGridWeek';
    const savedValue = localStorage.getItem(CalendarViewSettingsService.CALENDAR_VIEW_LOCAL_STORAGE_KEY);
    if (savedValue !== null) {
      switch (savedValue) {
        case 'dayGridMonth':
        case 'timeGridWeek':
        case 'timeGridDay':
          result = savedValue;
          break;
        default:
          break;
      }
    }
    return result;
  }

  changeViewIdentifier(viewIdentifier: ViewIdentifier) {
    localStorage.setItem(CalendarViewSettingsService.CALENDAR_VIEW_LOCAL_STORAGE_KEY, viewIdentifier);
    this.activeViewIdentifier.next(viewIdentifier);
  }
}
