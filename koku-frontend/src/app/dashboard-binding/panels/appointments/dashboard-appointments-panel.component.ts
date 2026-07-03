import { Component, inject, input, signal } from '@angular/core';
import { forkJoin, map, mergeMap, Observable } from 'rxjs';
import { get } from '../../../utils/get';
import { HttpClient } from '@angular/common/http';
import { toObservable } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs/operators';
import { MyUserDetailsService } from '../../../user/my-user-details.service';
import { ToastService } from '../../../toast/toast.service';
import dayjs from 'dayjs';
import { colorBorderClass } from '../../../utils/color.utils';

interface AppointmentItem {
  id: string;
  start: Date;
  time: string;
  topHeadline?: string;
  headline: string;
  subHeadline?: string;
  past: boolean;
  borderColorClass?: string;
  allDay?: boolean;
}

interface LoadedAppointmentListSource {
  listSource: KokuDto.DashboardAppointmentsPanelListSourceDto;
  listPage: KokuDto.ListPage;
}

@Component({
  selector: 'dashboard-appointments-panel',
  imports: [],
  templateUrl: './dashboard-appointments-panel.component.html',
})
export class DashboardAppointmentsPanelComponent {
  content = input.required<KokuDto.DashboardAppointmentsPanelDto>();
  appointments = signal<AppointmentItem[] | null>(null);
  loading = signal<boolean>(false);

  toastService = inject(ToastService);
  httpClient = inject(HttpClient);
  myUserDetailsService = inject(MyUserDetailsService);

  constructor() {
    toObservable(this.content).subscribe((content) => {
      this.loadAppointments(content);
    });
  }

  private loadAppointments(content: KokuDto.DashboardAppointmentsPanelDto): void {
    const listObservables = this.createListObservables(content);
    if (listObservables.length === 0) {
      this.appointments.set(null);
      return;
    }

    this.loading.set(true);
    forkJoin(listObservables)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => {
          this.updateAppointments(data);
        },
        error: () => {
          this.toastService.add('Fehler beim Laden der Daten', 'error', undefined, Number.POSITIVE_INFINITY);
        },
      });
  }

  private createListObservables(
    content: KokuDto.DashboardAppointmentsPanelDto,
  ): Observable<LoadedAppointmentListSource>[] {
    const listObservables: Observable<LoadedAppointmentListSource>[] = [];
    for (const currentListSource of content.listSources || []) {
      const listObservable = this.createListObservable(content, currentListSource);
      if (listObservable) {
        listObservables.push(listObservable);
      }
    }
    return listObservables;
  }

  private createListObservable(
    content: KokuDto.DashboardAppointmentsPanelDto,
    currentListSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
  ): Observable<LoadedAppointmentListSource> | undefined {
    const sourceUrl = currentListSource.sourceUrl;
    if (!sourceUrl) {
      return undefined;
    }

    const listQuery = this.createListQuery(content, currentListSource);
    const userIdFieldSelectionPath = currentListSource.userIdFieldSelectionPath;
    if (userIdFieldSelectionPath) {
      return this.myUserDetailsService.getCurrentUserDetailsCached().pipe(
        mergeMap((userDetails) => {
          this.addFieldSelection(listQuery, userIdFieldSelectionPath);
          this.addFieldPredicate(listQuery, userIdFieldSelectionPath, {
            searchExpression: String(userDetails.id),
            searchOperator: 'EQ',
          });
          return this.fetchList(currentListSource, sourceUrl, listQuery);
        }),
      );
    }

    return this.fetchList(currentListSource, sourceUrl, listQuery);
  }

  private createListQuery(
    content: KokuDto.DashboardAppointmentsPanelDto,
    currentListSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
  ): KokuDto.ListQuery {
    const listQuery: KokuDto.ListQuery = {
      fieldSelection: [],
      limit: 100,
      page: 0,
      fieldPredicates: {},
    };

    this.addDatePredicates(listQuery, content, currentListSource);
    this.addFieldSelection(listQuery, currentListSource.startTimeFieldSelectionPath);
    this.addFieldSelection(listQuery, currentListSource.endTimeFieldSelectionPath);
    this.addFieldSelection(listQuery, currentListSource.textFieldSelectionPath);
    this.addFieldSelection(listQuery, currentListSource.notesTextFieldSelectionPath);
    this.addDeletedPredicate(listQuery, currentListSource);
    return listQuery;
  }

  private addDatePredicates(
    listQuery: KokuDto.ListQuery,
    content: KokuDto.DashboardAppointmentsPanelDto,
    currentListSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
  ): void {
    if (currentListSource.startDateFieldSelectionPath && content.start) {
      this.addFieldSelection(listQuery, currentListSource.startDateFieldSelectionPath);
      this.addFieldPredicate(listQuery, currentListSource.startDateFieldSelectionPath, {
        searchExpression: dayjs(content.start).format('YYYY-MM-DD'),
        searchOperator: 'GREATER_OR_EQ',
        searchOperatorHint: currentListSource.searchOperatorHint,
      });
    }
    if (currentListSource.endDateFieldSelectionPath && content.end) {
      this.addFieldSelection(listQuery, currentListSource.endDateFieldSelectionPath);
      this.addFieldPredicate(listQuery, currentListSource.endDateFieldSelectionPath, {
        searchExpression: dayjs(content.end).format('YYYY-MM-DD'),
        searchOperator: 'LESS_OR_EQ',
        searchOperatorHint: currentListSource.searchOperatorHint,
      });
    }
  }

  private addDeletedPredicate(
    listQuery: KokuDto.ListQuery,
    currentListSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
  ): void {
    if (currentListSource.deletedFieldSelectionPath) {
      this.addFieldSelection(listQuery, currentListSource.deletedFieldSelectionPath);
      this.addFieldPredicate(listQuery, currentListSource.deletedFieldSelectionPath, {
        searchExpression: 'TRUE',
        searchOperator: 'EQ',
        negate: true,
      });
    }
  }

  private addFieldSelection(listQuery: KokuDto.ListQuery, fieldSelectionPath: string | undefined): void {
    if (fieldSelectionPath && !listQuery.fieldSelection?.includes(fieldSelectionPath)) {
      listQuery.fieldSelection?.push(fieldSelectionPath);
    }
  }

  private addFieldPredicate(
    listQuery: KokuDto.ListQuery,
    fieldSelectionPath: string,
    predicate: KokuDto.QueryPredicate,
  ): void {
    const fieldPredicates = listQuery.fieldPredicates || {};
    fieldPredicates[fieldSelectionPath] = {
      predicates: [predicate, ...(fieldPredicates[fieldSelectionPath]?.predicates || [])],
    };
    listQuery.fieldPredicates = fieldPredicates;
  }

  private fetchList(
    currentListSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
    sourceUrl: string,
    listQuery: KokuDto.ListQuery,
  ): Observable<LoadedAppointmentListSource> {
    return this.httpClient.post<KokuDto.ListPage>(sourceUrl, listQuery).pipe(
      map((listPage) => ({
        listSource: currentListSource,
        listPage,
      })),
    );
  }

  private updateAppointments(data: LoadedAppointmentListSource[]): void {
    const appointments = data.flatMap(({ listSource, listPage }) => this.createAppointmentItems(listSource, listPage));
    if (appointments.length === 0) {
      this.appointments.set(null);
      return;
    }

    appointments.sort((a, b) => a.start.getTime() - b.start.getTime());
    this.appointments.set(appointments);
  }

  private createAppointmentItems(
    listSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
    listPage: KokuDto.ListPage,
  ): AppointmentItem[] {
    return (listPage.results || []).map((currentListPageItem) =>
      this.createAppointmentItem(listSource, currentListPageItem),
    );
  }

  private createAppointmentItem(
    listSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
    currentListPageItem: KokuDto.ListItem,
  ): AppointmentItem {
    this.assertListItemCanBecomeAppointment(listSource, currentListPageItem);
    const date = this.createAppointmentDate(listSource, currentListPageItem);

    return {
      id: currentListPageItem.id!,
      start: date.toDate(),
      time: date.format('HH:mm'),
      past: date.isBefore(Date.now()),
      topHeadline: listSource.sourceItemText,
      headline: get(currentListPageItem.values, listSource.textFieldSelectionPath!),
      subHeadline: this.createSubHeadline(listSource, currentListPageItem),
      borderColorClass: colorBorderClass(listSource.sourceItemColor),
      allDay: listSource.allDay,
    };
  }

  private assertListItemCanBecomeAppointment(
    listSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
    currentListPageItem: KokuDto.ListItem,
  ): void {
    const itemDescription = this.describeListItem(currentListPageItem);
    if (!currentListPageItem.id) {
      throw new Error(`Missing id for item ${itemDescription}`);
    }
    if (!listSource.startDateFieldSelectionPath) {
      throw new Error(`Missing startDateFieldSelectionPath for item ${itemDescription}`);
    }
    if (!listSource.textFieldSelectionPath) {
      throw new Error(`Missing textFieldSelectionPath for item ${itemDescription}`);
    }
  }

  private createAppointmentDate(
    listSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
    currentListPageItem: KokuDto.ListItem,
  ): dayjs.Dayjs {
    const startDate = get(currentListPageItem.values, listSource.startDateFieldSelectionPath!);
    if (listSource.startTimeFieldSelectionPath) {
      const startTime = get(currentListPageItem.values, listSource.startTimeFieldSelectionPath);
      return dayjs(`${startDate}T${startTime}`);
    }
    return dayjs(startDate);
  }

  private createSubHeadline(
    listSource: KokuDto.DashboardAppointmentsPanelListSourceDto,
    currentListPageItem: KokuDto.ListItem,
  ): string | undefined {
    if (listSource.notesTextFieldSelectionPath === undefined) {
      return undefined;
    }
    return get(currentListPageItem.values, listSource.notesTextFieldSelectionPath, '');
  }

  private describeListItem(currentListPageItem: KokuDto.ListItem): string {
    return JSON.stringify(currentListPageItem);
  }
}
