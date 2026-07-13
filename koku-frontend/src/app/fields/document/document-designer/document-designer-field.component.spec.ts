import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';

const designerMock = vi.hoisted(() => {
  const instances: any[] = [];
  class Designer {
    template: any;
    callback?: (template: any) => void;
    getTemplate = vi.fn(() => this.template);
    updateTemplate = vi.fn((template: any) => (this.template = template));
    onChangeTemplate = vi.fn((callback: (template: any) => void) => (this.callback = callback));
    destroy = vi.fn();
    constructor(readonly options: any) {
      this.template = options.template;
      instances.push(this);
    }
  }
  return { Designer, instances };
});

vi.mock('@pdfme/ui', () => ({ Designer: designerMock.Designer }));

import { DocumentDesignerFieldComponent } from './document-designer-field.component';

describe('DocumentDesignerFieldComponent', () => {
  it('initializes, emits changed templates, updates values and destroys the designer', async () => {
    await TestBed.configureTestingModule({ imports: [DocumentDesignerFieldComponent] })
      .overrideComponent(DocumentDesignerFieldComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DocumentDesignerFieldComponent);
    const component = fixture.componentInstance;
    (component.designerRoot as any) = () => ({ nativeElement: document.createElement('div') });
    fixture.componentRef.setInput('name', 'template');
    fixture.componentRef.setInput('value', '');
    fixture.detectChanges();
    const designer = designerMock.instances.at(-1);
    expect(designer.options.template.schemas).toEqual([]);
    expect(designer.options.options).toEqual(expect.objectContaining({ zoomLevel: 1, lang: 'de' }));
    const changed = vi.fn();
    component.changed.subscribe(changed);
    designer.callback(designer.template);
    expect(changed).not.toHaveBeenCalled();
    const userTemplate = { basePdf: { width: 100 }, schemas: [[]] };
    designer.callback(userTemplate);
    expect(changed).toHaveBeenCalledWith(JSON.stringify(userTemplate));
    const external = { basePdf: { width: 200 }, schemas: [[{ type: 'text' }]] };
    fixture.componentRef.setInput('value', JSON.stringify(external));
    fixture.detectChanges();
    expect(designer.updateTemplate).toHaveBeenCalledWith(external);
    fixture.componentRef.setInput('value', JSON.stringify(external));
    fixture.detectChanges();
    expect(designer.updateTemplate).toHaveBeenCalledTimes(1);
    fixture.destroy();
    expect(designer.destroy).toHaveBeenCalled();
  });
});
