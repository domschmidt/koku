import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {CalendarOptions, FullCalendarComponent} from '@fullcalendar/angular';
import deLocale from '@fullcalendar/core/locales/de';
import {EventInput} from '@fullcalendar/common';

import * as moment from 'moment';
import {MatDialog} from '@angular/material/dialog';
import {
  CustomerAppointmentDetailsComponent,
  CustomerAppointmentDetailsData
} from '../customer/customer-appointment-details/customer-appointment-details.component';
import {
  CustomerInfoDialogComponent,
  CustomerInfoDialogData
} from '../customer/customer-info-dialog/customer-info-dialog.component';
import {AppointmentService} from './appointment.service';
import {
  PrivateAppointmentDetailsComponent,
  PrivateAppointmentDetailsData
} from '../user/private-appointment-details/private-appointment-details.component';
import {MatMenu, MatMenuTrigger} from '@angular/material/menu';
import {ResizeSensor} from 'css-element-queries';
import Holidays, {HolidaysTypes} from 'date-holidays';
import {MatSelectChange} from '@angular/material/select';
import {
  UserSelectionComponent,
  UserSelectionComponentData,
  UserSelectionComponentResponseData
} from '../user/user-selection/user-selection.component';
import {MyUserDetailsService} from '../user/my-user-details.service';
import {PrivateAppointmentService} from '../user/private-appointment-details/private-appointment.service';
import {CustomerAppointmentService} from '../customer-appointment.service';
import {SnackBarService} from '../snackbar/snack-bar.service';
import {CalendarViewSettingsService, ViewIdentifier} from '../calendar-view-toggle/calendar-view-settings.service';

interface CalendarSettings {
  privateAppointments: boolean;
  customerAppointments: boolean;
  customerBirthdays: boolean;
  holidays: boolean;
  holidayCountry: HolidaysTypes.Country;
}

interface ExtendedHolidays {
  countryAndStateString: string;
  holidayCountry: HolidaysTypes.Country;
}

@Component({
  selector: 'calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss']
})
export class CalendarComponent implements OnInit {

  private static readonly CALENDAR_LOCAL_STORAGE_KEY = 'lastCalendarView';
  @ViewChild('fullCalendarComponent') fullCalendarComponent: FullCalendarComponent | undefined;
  @ViewChild('calendarContextMenu') calendarContextMenu: MatMenu | undefined;
  @ViewChild('menuTrigger') menuTrigger: MatMenuTrigger | undefined;
  @ViewChild('pageWrapper', {
    read: ElementRef,
    static: true
  }) pageWrapper: ElementRef<HTMLDivElement> | undefined;
  positionX = 0;
  positionY = 0;
  loading = true;
  user: KokuDto.KokuUserDetailsDto | undefined;
  self: KokuDto.KokuUserDetailsDto | undefined;
  holidayCountriesAndStates: ExtendedHolidays[] = (() => {
    const result: ExtendedHolidays[] = [];

    const dateHoliday = new Holidays();

    const allCountries = dateHoliday.getCountries(); // e.g. {de: 'Deutschland', ...}
    for (const currentCountryKey of Object.keys(allCountries)) { // e.g. 'de'
      const currentCountryName = allCountries[currentCountryKey]; // e.g. 'Deutschland'

      const allCountryStates = dateHoliday.getStates(currentCountryKey);
      if (allCountryStates) {
        for (const currentCountryStateKey of Object.keys(allCountryStates)) { // e.g. 'RP'
          const currentCountryStateName = allCountryStates[currentCountryStateKey];

          result.push({
            countryAndStateString: currentCountryName + ' / ' + currentCountryStateName,
            holidayCountry: {
              country: currentCountryKey,
              state: currentCountryStateKey
            }
          });
        }
      } else {
        result.push({
          countryAndStateString: currentCountryName,
          holidayCountry: {
            country: currentCountryKey
          }
        });
      }
    }

    return result;
  })();

  calendarSettings: CalendarSettings = this.restoreViewSettings();

  humanReadableDateRange = '';
  calendarOptions: CalendarOptions;
  private dateFrom: Date | undefined;
  private dateTo: Date | undefined;

  @HostListener('window:keyup', ['$event'])
  keyEvent(event: KeyboardEvent): void {
    if (!this.dialog.openDialogs.length) {
      switch (event.key) {
        case 'ArrowUp':
          this.nextYear();
          break;
        case 'ArrowRight':
          this.next();
          break;
        case 'ArrowDown':
          this.prevYear();
          break;
        case 'ArrowLeft':
          this.prev();
          break;
      }
    }
  }

  constructor(private readonly dialog: MatDialog,
              private readonly appointmentService: AppointmentService,
              private readonly userDetailsService: MyUserDetailsService,
              private readonly privateAppointmentService: PrivateAppointmentService,
              private readonly customerAppointmentService: CustomerAppointmentService,
              private readonly snackBarService: SnackBarService,
              private readonly calendarViewSettingsService: CalendarViewSettingsService) {
    this.userDetailsService.getDetails().subscribe((userDetails) => {
      this.self = userDetails;
      this.user = userDetails;
    });
    this.calendarViewSettingsService.activeViewIdentifier.subscribe((viewIdentificator: ViewIdentifier) => {
      this.changeCalendarView(viewIdentificator);
    });
    this.calendarOptions = {
      initialView: calendarViewSettingsService.activeViewIdentifier.value,
      height: 'auto',
      allDaySlot: true,
      locale: deLocale,
      headerToolbar: false,
      dateClick: (event) => {
        let clientX = 0;
        let clientY = 0;
        if (event.jsEvent) {
          clientX = event.jsEvent.clientX;
          clientY = event.jsEvent.clientY;
          if ((event.jsEvent as any).changedTouches) {
            const firstTouchEvent: Touch = (event.jsEvent as any).changedTouches[0];
            clientX = firstTouchEvent.clientX;
            clientY = firstTouchEvent.clientY;
          }
        }
        if (event.allDay) {
          this.askForCreationType(clientX, clientY, event.date, moment(event.date).endOf('day').toDate());
        } else {
          this.askForCreationType(clientX, clientY, event.date);
        }
      },
      editable: true,
      eventDrop: (event) => {
        const content: KokuDto.ICalendarContentUnion = event.event.extendedProps as KokuDto.ICalendarContentUnion;
        if (content['@type'] === 'CustomerAppointment') {
          this.loading = true;
          this.customerAppointmentService.updateCustomerAppointmentTiming({
            '@type': 'CustomerAppointment',
            id: content.id,
            startDate: moment(event.event.start).format('YYYY-MM-DD'),
            startTime: moment(event.event.start).format('HH:mm')
          }).subscribe(() => {
            this.loading = false;
          }, () => {
            this.snackBarService.openCommonSnack('Speichern nicht möglich.');
            this.loading = false;
            event.revert();
          });
        } else if (content['@type'] === 'PrivateAppointment') {
          this.loading = true;
          this.privateAppointmentService.updatePrivateAppointmentTiming({
            '@type': 'PrivateAppointment',
            id: content.id,
            startDate: moment(event.event.start).format('YYYY-MM-DD'),
            startTime: moment(event.event.start).format('HH:mm'),
            endDate: moment(event.event.end).format('YYYY-MM-DD'),
            endTime: moment(event.event.end).format('HH:mm')
          }).subscribe(() => {
            this.loading = false;
          }, () => {
            this.snackBarService.openCommonSnack('Speichern nicht möglich.');
            this.loading = false;
            event.revert();
          });
        }
      },
      eventResize: (event) => {
        const content: KokuDto.ICalendarContentUnion = event.event.extendedProps as KokuDto.ICalendarContentUnion;
        if (content['@type'] === 'PrivateAppointment') {
          this.loading = true;
          this.privateAppointmentService.updatePrivateAppointmentTiming({
            '@type': 'PrivateAppointment',
            id: content.id,
            startDate: moment(event.event.start).format('YYYY-MM-DD'),
            startTime: moment(event.event.start).format('HH:mm'),
            endDate: moment(event.event.end).format('YYYY-MM-DD'),
            endTime: moment(event.event.end).format('HH:mm')
          }).subscribe(() => {
            this.loading = false;
          }, () => {
            this.snackBarService.openCommonSnack('Speichern nicht möglich.');
            this.loading = false;
            event.revert();
          });
        }
      },
      select: (event) => {
        let clientX = 0;
        let clientY = 0;
        if (event.jsEvent) {
          clientX = event.jsEvent.clientX;
          clientY = event.jsEvent.clientY;
          if ((event.jsEvent as any).changedTouches) {
            const firstTouchEvent: Touch = (event.jsEvent as any).changedTouches[0];
            clientX = firstTouchEvent.clientX;
            clientY = firstTouchEvent.clientY;
          }
        }
        this.askForCreationType(clientX, clientY, event.start, event.end);
      },
      viewDidMount: () => {
        setTimeout(() => {
          this.humanReadableDateRange = this.buildHumanReadableDateRange();
          setTimeout(() => {
              const scrollToNodeList = document.querySelectorAll('[data-time=\'07:00:00\']');
              if (scrollToNodeList && scrollToNodeList.length > 0) {
                scrollToNodeList[0].scrollIntoView();
              }
            }
          );
        });
      },
      eventMouseEnter: (args) => {
        args.el.classList.add('calendar-event--hover');
      },
      eventMouseLeave: (args) => {
        args.el.classList.remove('calendar-event--hover');
      },
      eventClick: (args) => {
        const content: KokuDto.ICalendarContentUnion = args.event.extendedProps as KokuDto.ICalendarContentUnion;
        if (content['@type'] === 'CustomerAppointment') {
          const dialogData: CustomerAppointmentDetailsData = {
            customerAppointmentId: content.id
          };
          const dialogRef = this.dialog.open(CustomerAppointmentDetailsComponent, {
            data: dialogData,
            autoFocus: false,
            closeOnNavigation: false,
            position: {
              top: '20px'
            }
          });
          dialogRef.afterClosed().subscribe(() => {
            this.refreshCalendarEvents();
          });
        } else if (content['@type'] === 'CustomerBirthday') {
          const dialogData: CustomerInfoDialogData = {
            customerId: content.id
          };
          const dialogRef = this.dialog.open(CustomerInfoDialogComponent, {
            data: dialogData,
            closeOnNavigation: false,
            position: {
              top: '20px'
            }
          });
          dialogRef.afterClosed().subscribe(() => {
            this.refreshCalendarEvents();
          });
        } else if (content['@type'] === 'PrivateAppointment') {
          const dialogData: PrivateAppointmentDetailsData = {
            privateAppointmentId: content.id
          };
          const dialogRef = this.dialog.open(PrivateAppointmentDetailsComponent, {
            data: dialogData,
            closeOnNavigation: false,
            position: {
              top: '20px'
            }
          });
          dialogRef.afterClosed().subscribe(() => {
            this.refreshCalendarEvents();
          });
        }
      },
      nowIndicator: true,
      businessHours: {
        daysOfWeek: [1, 2, 3, 4, 5], // Monday - Thursday
        startTime: '08:00:00', // a start time (10am in this example)
        endTime: '20:00:00', // an end time (6pm in this example)
      },
      selectable: true,
      events: (
        args,
        successCallback,
        failureCallback
      ) => {
        this.loading = true;
        this.appointmentService.loadAppointments(
          {
            start: args.start,
            end: args.end,
            privateAppointments: this.calendarSettings.privateAppointments,
            customerBirthdays: this.calendarSettings.customerBirthdays,
            customerAppointments: this.calendarSettings.customerAppointments,
            userId: (this.user && this.user.id && this.self && this.user.id !== this.self.id) ? this.user.id : undefined
          }
        ).subscribe((result) => {
          this.loading = false;
          successCallback([
            ...this.transformAppointmentsToCalendarEvents(result),
            ...(this.calendarSettings.holidays ? this.defineHolidays(args.start, args.end) : [])
          ]);
        }, (error) => {
          this.loading = false;
          failureCallback(error);
        });
      },
    };
  }


  createNewCustomerAppointment(): void {
    const dialogData: CustomerAppointmentDetailsData = {
      startDate: this.dateFrom ? moment(this.dateFrom).format('YYYY-MM-DD') : undefined,
      startTime: this.dateFrom ? moment(this.dateFrom).format('HH:mm') : undefined
    };
    const dialogRef = this.dialog.open(CustomerAppointmentDetailsComponent, {
      data: dialogData,
      autoFocus: false,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
    dialogRef.afterClosed().subscribe(() => {
      this.refreshCalendarEvents();
    });
  }

  createNewPersonalAppointment(): void {
    const dialogData: PrivateAppointmentDetailsData = {
      startDate: this.dateFrom ? moment(this.dateFrom).format('YYYY-MM-DD') : undefined,
      startTime: this.dateFrom ? moment(this.dateFrom).format('HH:mm') : undefined,
      endDate: this.dateTo
        ? moment(this.dateTo).format('YYYY-MM-DD')
        : this.dateFrom
          ? moment(this.dateFrom).format('YYYY-MM-DD')
          : undefined,
      endTime: this.dateTo
        ? moment(this.dateTo).format('HH:mm')
        : this.dateFrom
          ? moment(this.dateFrom).add(30, 'minutes').format('HH:mm')
          : undefined,
    };
    const dialogRef = this.dialog.open(PrivateAppointmentDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
    dialogRef.afterClosed().subscribe(() => {
      this.refreshCalendarEvents();
    });
  }

  askForCreationType(clientX: number, clientY: number, dateFrom?: Date, dateTo?: Date): void {
    if (this.menuTrigger) {
      this.positionX = clientX;
      this.positionY = clientY;
      this.menuTrigger.openMenu();
      if (dateFrom) {
        this.dateFrom = dateFrom;
      } else {
        delete this.dateFrom;
      }
      if (dateTo) {
        this.dateTo = dateTo;
      } else {
        delete this.dateTo;
      }
    }
  }

  ngOnInit(): void {
    if (this.pageWrapper) {
      new ResizeSensor(this.pageWrapper.nativeElement, () => {
        this.menuTrigger?.closeMenu();
      });
    }
  }

  private transformAppointmentsToCalendarEvents(apiResponse: KokuDto.ICalendarContentUnion[]): EventInput[] {
    const result: EventInput[] = [];

    for (const currentAppointment of apiResponse) {
      if (currentAppointment['@type'] === 'CustomerAppointment') {
        let approxEnd;
        if (currentAppointment.approximatelyDuration) {
          const currentDuration = moment.duration(currentAppointment.approximatelyDuration);
          const approxEndMoment = moment(currentAppointment.startDate + 'T' + currentAppointment.startTime).add(currentDuration);
          const approxEndDate = approxEndMoment.format('YYYY-MM-DD');
          const approxEndTime = approxEndMoment.format('HH:mm:ss');
          approxEnd = approxEndDate + 'T' + approxEndTime;
        } else {
          approxEnd = currentAppointment.startDate + 'T' + moment(currentAppointment.startTime, 'HH:mm').format('HH:mm:ss');
        }

        result.push({
          title: (currentAppointment.customer?.firstName
            ? currentAppointment.customer?.firstName
            : ''
            )
            + ' '
            + (currentAppointment.customer?.lastName
              ? currentAppointment.customer?.lastName
              : ''
            ),
          start: currentAppointment.startDate + 'T' + moment(currentAppointment.startTime, 'HH:mm').format('HH:mm:ss'),
          end: approxEnd,
          id: String(currentAppointment.id),
          extendedProps: currentAppointment,
          classNames: [
            'calendar-event',
            'calendar-event--clickable',
            'customer-appointment',
            'clickable-event'
          ],
          durationEditable: false
        });
      } else if (currentAppointment['@type'] === 'CustomerBirthday') {
        result.push({
          title: 'Geburtstag ' + currentAppointment.firstName + ' ' + currentAppointment.lastName,
          id: String(currentAppointment.id),
          allDay: true,
          classNames: [
            'calendar-event',
            'calendar-event--clickable',
            'customer-birthday',
          ],
          extendedProps: currentAppointment,
          rrule: {
            freq: 'yearly',
            dtstart: moment.utc(currentAppointment.birthday).toDate()
          },
          editable: false
        });
      } else if (currentAppointment['@type'] === 'PrivateAppointment') {
        result.push({
          title: currentAppointment.description,
          id: String(currentAppointment.id),
          start: currentAppointment.startDate + 'T' + moment(currentAppointment.startTime, 'HH:mm').format('HH:mm:ss'),
          end: currentAppointment.endDate && currentAppointment.endTime
            ? currentAppointment.endDate + 'T' + moment(currentAppointment.endTime, 'HH:mm').format('HH:mm:ss')
            : currentAppointment.startDate + 'T' + moment(currentAppointment.startTime, 'HH:mm').add(30, 'minutes').format('HH:mm:ss'),
          classNames: [
            'calendar-event',
            'calendar-event--clickable',
            'private-appointment',
          ],
          extendedProps: currentAppointment
        });
      }
    }

    return result;
  }

  private restoreViewSettings(): CalendarSettings {
    let result: CalendarSettings = {
      privateAppointments: true,
      customerAppointments: true,
      customerBirthdays: true,
      holidays: true,
      holidayCountry: {
        country: 'DE',
        state: 'RP'
      }
    };
    const savedValue = localStorage.getItem(CalendarComponent.CALENDAR_LOCAL_STORAGE_KEY);
    if (savedValue !== null) {
      try {
        const castedInsecureVal = JSON.parse(savedValue) as CalendarSettings;
        result = {
          customerAppointments: castedInsecureVal.customerAppointments,
          privateAppointments: castedInsecureVal.privateAppointments,
          customerBirthdays: castedInsecureVal.customerBirthdays,
          holidays: castedInsecureVal.holidays,
          holidayCountry: castedInsecureVal.holidayCountry
        };
      } catch (e) {

      }
    }
    return result;
  }

  refreshCalendarEvents(): void {
    if (this.fullCalendarComponent) {
      this.fullCalendarComponent.getApi().refetchEvents();
    }
  }

  changeCalendarView(viewIdentificator: ViewIdentifier): void {
    if (this.fullCalendarComponent) {
      this.fullCalendarComponent.getApi().changeView(viewIdentificator);
      this.humanReadableDateRange = this.buildHumanReadableDateRange();
    }
  }

  buildHumanReadableDateRange(): string {
    let result = '';
    if (this.fullCalendarComponent) {
      const viewType: ViewIdentifier = this.fullCalendarComponent.getApi().view.type as ViewIdentifier;
      const startMoment = moment(this.fullCalendarComponent.getApi().view.currentStart);
      const endMoment = moment(this.fullCalendarComponent.getApi().view.currentEnd).subtract(1, 'ms');
      const startDay = startMoment.format('D');
      const endDay = endMoment.format('D');
      const currentMonth = endMoment.format('MMM');
      const currentYear = startMoment.format('YYYY');
      switch (viewType) {
        case 'dayGridMonth':
          result = `${startDay} - ${endDay}. ${currentMonth} ${currentYear}`;
          break;
        case 'timeGridWeek':
          result = `${startDay} - ${endDay}. ${currentMonth} ${currentYear}`;
          break;
        case 'timeGridDay':
          result = `${startDay} . ${currentMonth} ${currentYear}`;
          break;
        default:
          break;
      }
    }
    return result;
  }

  prevYear(): void {
    if (this.fullCalendarComponent) {
      this.fullCalendarComponent.getApi().prevYear();
      this.humanReadableDateRange = this.buildHumanReadableDateRange();
    }
  }

  prev(): void {
    if (this.fullCalendarComponent) {
      this.fullCalendarComponent.getApi().prev();
      this.humanReadableDateRange = this.buildHumanReadableDateRange();
    }
  }

  next(): void {
    if (this.fullCalendarComponent) {
      this.fullCalendarComponent.getApi().next();
      this.humanReadableDateRange = this.buildHumanReadableDateRange();
    }
  }

  nextYear(): void {
    if (this.fullCalendarComponent) {
      this.fullCalendarComponent.getApi().nextYear();
      this.humanReadableDateRange = this.buildHumanReadableDateRange();
    }
  }

  toggleLoadPrivateAppointments(): void {
    this.calendarSettings.privateAppointments = !this.calendarSettings.privateAppointments;
    this.refreshCalendarEvents();
    localStorage.setItem(CalendarComponent.CALENDAR_LOCAL_STORAGE_KEY, JSON.stringify(this.calendarSettings));
  }

  toggleLoadCustomerAppointments(): void {
    this.calendarSettings.customerAppointments = !this.calendarSettings.customerAppointments;
    this.refreshCalendarEvents();
    localStorage.setItem(CalendarComponent.CALENDAR_LOCAL_STORAGE_KEY, JSON.stringify(this.calendarSettings));
  }

  toggleLoadCustomerBirthdays(): void {
    this.calendarSettings.customerBirthdays = !this.calendarSettings.customerBirthdays;
    this.refreshCalendarEvents();
    localStorage.setItem(CalendarComponent.CALENDAR_LOCAL_STORAGE_KEY, JSON.stringify(this.calendarSettings));
  }

  toggleLoadHolidays(): void {
    this.calendarSettings.holidays = !this.calendarSettings.holidays;
    this.refreshCalendarEvents();
    localStorage.setItem(CalendarComponent.CALENDAR_LOCAL_STORAGE_KEY, JSON.stringify(this.calendarSettings));
  }

  private defineHolidays(start: Date, end: Date): EventInput[] {
    const result: EventInput[] = [];
    const holidays = new Holidays(this.calendarSettings.holidayCountry);

    const rangeOfYears = (start: number, end: number) => {
      return Array<number>(end - start + 1)
        .fill(start)
        .map((year, index) => year + index);
    };
    const years = rangeOfYears(start.getFullYear(), end.getFullYear());

    for (const currentYear of years) {
      for (const holiday of holidays.getHolidays(currentYear)) {
        result.push({
          title: holiday.name,
          start: holiday.start,
          end: holiday.end,
          editable: false,
          allDay: true
        });
      }
    }
    return result;
  }

  changeHolidayCountry($event: MatSelectChange): void {
    this.calendarSettings.holidayCountry = $event.value;
    this.refreshCalendarEvents();
    localStorage.setItem(CalendarComponent.CALENDAR_LOCAL_STORAGE_KEY, JSON.stringify(this.calendarSettings));
  }

  compareHolidayCountry(o1: HolidaysTypes.Country, o2: HolidaysTypes.Country): boolean {
    if (o1.country === o2.country) {
      if (o1.state === null && o2.state == null) {
        return true;
      } else if (o1.state === o2.state) {
        return true;
      }
    }
    return false;
  }

  selectUser(): void {
    const dialogData: UserSelectionComponentData = {};
    const dialogRef = this.dialog.open(UserSelectionComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });

    dialogRef.afterClosed().subscribe((result: UserSelectionComponentResponseData) => {
      if (result && result.user) {
        this.user = result.user;
        this.refreshCalendarEvents();
      }
    });
  }
}
