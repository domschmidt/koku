import {Component, inject, input, signal} from '@angular/core';
import {forkJoin, map, mergeMap, Observable} from 'rxjs';
import {get} from '../../../utils/get';
import {HttpClient} from '@angular/common/http';
import {toObservable} from '@angular/core/rxjs-interop';
import {finalize, tap} from 'rxjs/operators';
import {MyUserDetailsService} from '../../../user/my-user-details.service';
import {ToastService} from '../../../toast/toast.service';
import dayjs from 'dayjs';

type AppointmentItem = {
  id: string,
  start: Date,
  time: string,
  topHeadline?: string,
  headline: string,
  subHeadline?: string,
  past: boolean,
  borderColorClass?: string,
  allDay?: boolean,
};

type COLORS = keyof KokuDto.KokuColorEnum;

@Component({
  selector: 'dashboard-appointments-panel',
  imports: [],
  templateUrl: './dashboard-appointments-panel.component.html',
  styleUrl: './dashboard-appointments-panel.component.css'
})
export class DashboardAppointmentsPanelComponent {

  content = input.required<KokuDto.DashboardAppointmentsPanelDto>();
  appointments = signal<AppointmentItem[] | null>(null);
  loading = signal<boolean>(false);

  toastService = inject(ToastService);
  httpClient = inject(HttpClient);
  myUserDetailsService = inject(MyUserDetailsService);

  private colorClass: Record<Partial<KokuDto.KokuColorEnum>, string> = {
    "PRIMARY": 'border-primary-600',
    "SECONDARY": 'border-secondary-600',
    "ACCENT": 'border-accent-600',
    "INFO": 'border-info-600',
    "SUCCESS": 'border-success-600',
    "WARNING": 'border-warning-600',
    "ERROR": 'border-error-600',
    "RED": 'border-red-600',
    "ORANGE": 'border-orange-600',
    "AMBER": 'border-amber-600',
    "YELLOW": 'border-yellow-600',
    "LIME": 'border-lime-600',
    "GREEN": 'border-green-600',
    "EMERALD": 'border-emerald-600',
    "TEAL": 'border-teal-600',
    "CYAN": 'border-cyan-600',
    "SKY": 'border-sky-600',
    "BLUE": 'border-blue-600',
    "INDIGO": 'border-indigo-600',
    "VIOLET": 'border-violet-600',
    "PURPLE": 'border-purple-600',
    "FUCHSIA": 'border-fuchsia-600',
    "PINK": 'border-pink-600',
    "ROSE": 'border-rose-600',
    "SLATE": 'border-slate-600',
    "GRAY": 'border-gray-600',
    "ZINC": 'border-zinc-600',
    "NEUTRAL": 'border-neutral-600',
    "STONE": 'border-stone-600',
  }

  constructor() {
    toObservable(this.content).subscribe((content) => {
      const listObservables: Observable<[KokuDto.DashboardAppointmentsPanelListSourceDto, KokuDto.ListPage]>[] = [];

      for (const currentListSource of content.listSources || []) {
        const fieldSelection: Set<string> = new Set<string>();
        const fieldPredicates: { [index: string]: KokuDto.ListFieldQuery } = {};
        if (currentListSource.startDateFieldSelectionPath && content.start) {
          fieldSelection.add(currentListSource.startDateFieldSelectionPath);
          fieldPredicates[currentListSource.startDateFieldSelectionPath] = {
            predicates: [
              {
                searchExpression: dayjs(content.start).format('YYYY-MM-DD'),
                searchOperator: 'GREATER_OR_EQ',
                searchOperatorHint: currentListSource.searchOperatorHint
              },
              ...((fieldPredicates[currentListSource.startDateFieldSelectionPath] || {}).predicates || [])
            ]
          }
        }
        if (currentListSource.endDateFieldSelectionPath && content.end) {
          fieldSelection.add(currentListSource.endDateFieldSelectionPath);
          fieldPredicates[currentListSource.endDateFieldSelectionPath] = {
            predicates: [
              {
                searchExpression: dayjs(content.end).format('YYYY-MM-DD'),
                searchOperator: 'LESS_OR_EQ',
                searchOperatorHint: currentListSource.searchOperatorHint
              },
              ...((fieldPredicates[currentListSource.endDateFieldSelectionPath] || {}).predicates || [])
            ]
          }
        }
        if (currentListSource.startTimeFieldSelectionPath) {
          fieldSelection.add(currentListSource.startTimeFieldSelectionPath);
        }
        if (currentListSource.endTimeFieldSelectionPath) {
          fieldSelection.add(currentListSource.endTimeFieldSelectionPath);
        }
        if (currentListSource.textFieldSelectionPath) {
          fieldSelection.add(currentListSource.textFieldSelectionPath);
        }
        if (currentListSource.notesTextFieldSelectionPath) {
          fieldSelection.add(currentListSource.notesTextFieldSelectionPath);
        }
        if (currentListSource.deletedFieldSelectionPath) {
          fieldSelection.add(currentListSource.deletedFieldSelectionPath);
          fieldPredicates[currentListSource.deletedFieldSelectionPath] = {
            predicates: [
              {
                searchExpression: 'TRUE',
                searchOperator: 'EQ',
                negate: true
              },
              ...((fieldPredicates[currentListSource.deletedFieldSelectionPath] || {}).predicates || [])
            ]
          }
        }
        if (currentListSource.sourceUrl) {
          const currentListSourceUrl = currentListSource.sourceUrl;

          const userIdFieldSelectionPath = currentListSource.userIdFieldSelectionPath;
          if (userIdFieldSelectionPath) {
            listObservables.push(this.myUserDetailsService.getCurrentUserDetailsCached().pipe(
              mergeMap((userDetails) => {
                fieldSelection.add(userIdFieldSelectionPath);
                fieldPredicates[userIdFieldSelectionPath] = {
                  predicates: [
                    {
                      searchExpression: String(userDetails.id),
                      searchOperator: 'EQ',
                    },
                    ...((fieldPredicates[userIdFieldSelectionPath] || {}).predicates || [])
                  ]
                }

                return this.httpClient.post<KokuDto.ListPage>(currentListSourceUrl, {
                  fieldSelection: [...fieldSelection],
                  limit: 100,
                  page: 0,
                  fieldPredicates: fieldPredicates
                }).pipe(map<KokuDto.ListPage, [KokuDto.DashboardAppointmentsPanelListSourceDto, KokuDto.ListPage]>(value => [currentListSource, value]));
              })
            ));
          } else {
            listObservables.push(this.httpClient.post<KokuDto.ListPage>(currentListSourceUrl, {
              fieldSelection: [...fieldSelection],
              limit: 100,
              page: 0,
              fieldPredicates: fieldPredicates
            }).pipe(map<KokuDto.ListPage, [KokuDto.DashboardAppointmentsPanelListSourceDto, KokuDto.ListPage]>(value => [currentListSource, value])));
          }

        }
      }

      forkJoin(listObservables)
        .pipe(
          tap(value => {
            this.loading.set(true);
          }),
          finalize(() => {
            this.loading.set(false);
          })
        )
        .subscribe((data: [KokuDto.DashboardAppointmentsPanelListSourceDto, KokuDto.ListPage][]) => {
          const appointments: AppointmentItem[] = [];
          for (const currentResultData of data || []) {
            const listSource = currentResultData[0];
            const listPage = currentResultData[1];
            for (const currentListPageItem of listPage.results || []) {
              if (!currentListPageItem.id) {
                throw new Error(`Missing id for item ${currentListPageItem})`);
              }
              if (!listSource.startDateFieldSelectionPath) {
                throw new Error(`Missing startDateFieldSelectionPath for item ${currentListPageItem})`);
              }
              if (!listSource.textFieldSelectionPath) {
                throw new Error(`Missing textFieldSelectionPath for item ${currentListPageItem})`);
              }

              let date = dayjs(get(currentListPageItem.values, listSource.startDateFieldSelectionPath));
              if (listSource.startTimeFieldSelectionPath) {
                date = dayjs(get(currentListPageItem.values, listSource.startDateFieldSelectionPath) + 'T' + get(currentListPageItem.values, listSource.startTimeFieldSelectionPath))
              }

              appointments.push({
                id: currentListPageItem.id,
                start: date.toDate(),
                time: date.format('HH:mm'),
                past: date.isBefore(Date.now()),
                topHeadline: listSource.sourceItemText,
                headline: get(currentListPageItem.values, listSource.textFieldSelectionPath),
                subHeadline: listSource.notesTextFieldSelectionPath !== undefined ? get(currentListPageItem.values, listSource.notesTextFieldSelectionPath, '') : undefined,
                borderColorClass: listSource.sourceItemColor ? this.colorClass[listSource.sourceItemColor] : undefined,
                allDay: listSource.allDay
              });
            }
          }
          if (appointments && appointments.length > 0) {
            this.appointments.set(appointments.sort((a, b) => {
              return a.start.getTime() - b.start.getTime();
            }));
          } else {
            this.appointments.set(null);
          }
        }, () => {
          this.toastService.add("Fehler beim Laden der Daten", 'error', undefined, Number.POSITIVE_INFINITY);
        });
    });
  }

}
