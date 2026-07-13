import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { ModalService } from '../../modal/modal.service';
import { ToastService } from '../../toast/toast.service';
import { ListItemActionComponent } from './list-item-action.component';

describe('ListItemActionComponent', () => {
  it('constructs action state with injected collaborators', async () => {
    await TestBed.configureTestingModule({
      imports: [ListItemActionComponent],
      providers: [
        { provide: HttpClient, useValue: {} },
        { provide: ModalService, useValue: {} },
        { provide: ToastService, useValue: {} },
      ],
    })
      .overrideComponent(ListItemActionComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListItemActionComponent);
    fixture.componentRef.setInput('register', {});
    fixture.componentRef.setInput('listRegister', []);
    fixture.componentRef.setInput('contentSetup', {});
    fixture.detectChanges();
    expect(fixture.componentInstance.componentRef).toBeTruthy();
  });
});
