import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ToastService } from '../../../toast/toast.service';
import { MyUserDetailsService } from '../../../user/my-user-details.service';
import { DashboardAppointmentsPanelComponent } from './dashboard-appointments-panel.component';

describe('DashboardAppointmentsPanelComponent', () => {
  it('queries all sources, scopes user data and maps sorted appointment cards', async () => {
    const http = {
      post: vi.fn((url: string, query: unknown) => {
        void query;
        return of({
          results:
            url === '/private'
              ? [
                  {
                    id: 'private-1',
                    values: { start: '2027-01-03', time: '09:30', text: 'Internal', deleted: false },
                  },
                ]
              : [
                  {
                    id: 'customer-1',
                    values: {
                      start: '2027-01-02',
                      time: '10:15',
                      end: '2027-01-02',
                      text: 'Customer visit',
                      notes: 'Bring samples',
                      deleted: false,
                    },
                  },
                ],
        });
      }),
    };
    const userDetails = { getCurrentUserDetailsCached: vi.fn(() => of({ id: 7 })) };
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [DashboardAppointmentsPanelComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: MyUserDetailsService, useValue: userDetails },
        { provide: ToastService, useValue: toast },
      ],
    })
      .overrideComponent(DashboardAppointmentsPanelComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DashboardAppointmentsPanelComponent);
    fixture.componentRef.setInput('content', {
      start: '2027-01-01',
      end: '2027-01-31',
      listSources: [
        {
          sourceUrl: '/customer',
          startDateFieldSelectionPath: 'start',
          endDateFieldSelectionPath: 'end',
          startTimeFieldSelectionPath: 'time',
          textFieldSelectionPath: 'text',
          notesTextFieldSelectionPath: 'notes',
          deletedFieldSelectionPath: 'deleted',
          sourceItemText: 'Customer',
          sourceItemColor: 'red',
          searchOperatorHint: 'DATE',
        },
        {
          sourceUrl: '/private',
          startDateFieldSelectionPath: 'start',
          startTimeFieldSelectionPath: 'time',
          textFieldSelectionPath: 'text',
          userIdFieldSelectionPath: 'user.id',
          deletedFieldSelectionPath: 'deleted',
          allDay: true,
        },
        { textFieldSelectionPath: 'ignored' },
      ],
    });
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(http.post).toHaveBeenCalledTimes(2);
    const customerQuery = http.post.mock.calls.find(([url]) => url === '/customer')?.[1] as any;
    expect(customerQuery.fieldSelection).toEqual(
      expect.arrayContaining(['start', 'end', 'time', 'text', 'notes', 'deleted']),
    );
    expect(customerQuery.fieldPredicates.start.predicates[0]).toEqual(
      expect.objectContaining({ searchExpression: '2027-01-01', searchOperator: 'GREATER_OR_EQ' }),
    );
    expect(customerQuery.fieldPredicates.deleted.predicates[0]).toEqual(
      expect.objectContaining({ searchExpression: 'TRUE', negate: true }),
    );
    const privateQuery = http.post.mock.calls.find(([url]) => url === '/private')?.[1] as any;
    expect(privateQuery.fieldPredicates['user.id'].predicates[0]).toEqual({
      searchExpression: '7',
      searchOperator: 'EQ',
    });
    expect(component.loading()).toBe(false);
    expect(component.appointments()?.map((appointment) => appointment.id)).toEqual(['customer-1', 'private-1']);
    expect(component.appointments()?.[0]).toEqual(
      expect.objectContaining({
        time: '10:15',
        headline: 'Customer visit',
        subHeadline: 'Bring samples',
        topHeadline: 'Customer',
      }),
    );
    expect(component.appointments()?.[1].allDay).toBe(true);
    expect(toast.add).not.toHaveBeenCalled();
    fixture.destroy();
  });

  it('handles empty and failed source collections', async () => {
    const toast = { add: vi.fn() };
    const http = { post: vi.fn(() => throwError(() => new Error('offline'))) };
    await TestBed.configureTestingModule({
      imports: [DashboardAppointmentsPanelComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: MyUserDetailsService, useValue: { getCurrentUserDetailsCached: vi.fn() } },
        { provide: ToastService, useValue: toast },
      ],
    })
      .overrideComponent(DashboardAppointmentsPanelComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DashboardAppointmentsPanelComponent);
    fixture.componentRef.setInput('content', { listSources: [] });
    fixture.detectChanges();
    expect(fixture.componentInstance.appointments()).toBeNull();

    fixture.componentRef.setInput('content', {
      listSources: [
        {
          sourceUrl: '/failed',
          startDateFieldSelectionPath: 'start',
          textFieldSelectionPath: 'text',
        },
      ],
    });
    fixture.detectChanges();
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Laden der Daten', 'error', undefined, Number.POSITIVE_INFINITY);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('rejects list items that cannot become appointments', async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardAppointmentsPanelComponent],
      providers: [
        { provide: HttpClient, useValue: { post: vi.fn() } },
        { provide: MyUserDetailsService, useValue: {} },
        { provide: ToastService, useValue: { add: vi.fn() } },
      ],
    })
      .overrideComponent(DashboardAppointmentsPanelComponent, { set: { template: '' } })
      .compileComponents();
    const component = TestBed.createComponent(DashboardAppointmentsPanelComponent).componentInstance as any;
    component.updateAppointments([]);
    expect(component.appointments()).toBeNull();
    expect(() =>
      component.createAppointmentItem(
        { startDateFieldSelectionPath: 'start', textFieldSelectionPath: 'text' },
        { values: {} },
      ),
    ).toThrow('Missing id');
    expect(() => component.createAppointmentItem({ textFieldSelectionPath: 'text' }, { id: '1', values: {} })).toThrow(
      'Missing startDateFieldSelectionPath',
    );
    expect(() =>
      component.createAppointmentItem({ startDateFieldSelectionPath: 'start' }, { id: '1', values: {} }),
    ).toThrow('Missing textFieldSelectionPath');
    expect(
      component.createAppointmentItem(
        { startDateFieldSelectionPath: 'start', textFieldSelectionPath: 'text' },
        { id: '1', values: { start: '2027-02-01', text: 'All day' } },
      ),
    ).toEqual(expect.objectContaining({ time: '00:00', subHeadline: undefined }));
  });
});
