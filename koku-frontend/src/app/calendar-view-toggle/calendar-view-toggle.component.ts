import {Component} from '@angular/core';
import {CalendarViewSettingsService, ViewIdentifier} from "./calendar-view-settings.service";

@Component({
  selector: 'calendar-view-toggle',
  templateUrl: './calendar-view-toggle.component.html',
  styleUrls: ['./calendar-view-toggle.component.scss']
})
export class CalendarViewToggleComponent {

  viewIdentifier: ViewIdentifier | null = null;

  constructor(
    private readonly calendarViewSettingsService: CalendarViewSettingsService
  ) {
    this.calendarViewSettingsService.activeViewIdentifier.subscribe((viewIdentifier) => {
      this.viewIdentifier = viewIdentifier;
    })
  }

  changeCalendarView(viewIdentifier: ViewIdentifier) {
    this.calendarViewSettingsService.changeViewIdentifier(viewIdentifier);
  }
}
