import {
  Component,
  ComponentRef,
  ElementRef,
  inject,
  InjectionToken,
  input,
  OnChanges,
  OnDestroy,
  output,
  signal,
  SimpleChanges,
  TemplateRef,
  WritableSignal,
} from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ToastService } from '../toast/toast.service';
import { ContainerRendererComponent } from './container-renderer/container-renderer.component';
import { Observable, Subject, Subscriber, Subscription } from 'rxjs';
import { OutletDirective } from '../portal/outlet.directive';
import { set } from '../utils/set';
import { get } from '../utils/get';

export interface FormularFieldOverride {
  value: any;
  fieldId: string;
  disable?: boolean;
}

export interface FormularSourceOverride {
  value: any;
  path: string;
}

export type ContainerRegistrationType = Record<
  string,
  {
    config: KokuDto.AbstractFormContainer;
  }
>;

export type LayoutRegistrationType = Record<
  string,
  {
    config: KokuDto.AbstractFormLayout;
  }
>;

export type FormularFieldRegistrationType = Record<
  string,
  {
    value: WritableSignal<any>;
    disabledCauses: WritableSignal<Set<string>>;
    requiredCauses: WritableSignal<Set<string>>;
    readonlyCauses: WritableSignal<Set<string>>;
    loadingCauses: WritableSignal<Set<string>>;
    config: KokuDto.AbstractFormField<any>;
    instance?: ComponentRef<any>;
    fieldEventBus: Subject<{ eventName: FieldEvent; payload?: any }>;
  }
>;
export type FieldEvent =
  | 'onChange'
  | 'onInput'
  | 'onFocus'
  | 'onBlur'
  | 'onClick'
  | 'onFocusPrependOuter'
  | 'onBlurPrependOuter'
  | 'onClickPrependOuter'
  | 'onFocusPrependInner'
  | 'onBlurPrependInner'
  | 'onClickPrependInner'
  | 'onFocusAppendInner'
  | 'onBlurAppendInner'
  | 'onClickAppendInner'
  | 'onFocusAppendOuter'
  | 'onBlurAppendOuter'
  | 'onClickAppendOuter'
  | 'onInit';
export type ButtonRegistrationType = Record<
  string,
  {
    config: KokuDto.AbstractFormButton;
    loadingCauses: Set<string>;
    disabledCauses: Set<string>;
    buttonEventBus: Subject<{
      eventName: ButtonEvent;
      payload?: any;
    }>;
  }
>;
export type ButtonEvent = 'onClick';

export interface FormularContentStates {
  fields: FormularFieldRegistrationType;
  containers: ContainerRegistrationType;
  buttons: ButtonRegistrationType;
  layouts: LayoutRegistrationType;
}

export interface FormularContentSetup {
  buttonRegistry: Partial<
    Record<
      KokuDto.AbstractFormButton['@type'] | string,
      {
        componentType: any;
        stateInitializer: (formularContent: KokuDto.AbstractFormButton) => FormularContentStates;
        inputBindings?(instance: any, formularContent: KokuDto.AbstractFormButton): Record<string, any>;
        outputBindings?(instance: any, formularContent: KokuDto.AbstractFormButton): Record<string, any>;
      }
    >
  >;
  layoutRegistry: Partial<
    Record<
      KokuDto.AbstractFormLayout['@type'] | string,
      {
        componentType: any;
        stateInitializer: (formularContent: KokuDto.AbstractFormLayout) => FormularContentStates;
        inputBindings?(instance: any, formularContent: KokuDto.AbstractFormLayout): Record<string, any>;
        outputBindings?(instance: any, formularContent: KokuDto.AbstractFormLayout): Record<string, any>;
      }
    >
  >;
  fieldRegistry: Partial<
    Record<
      KokuDto.AbstractFormField<any>['@type'] | string,
      {
        componentType: any;
        stateInitializer: (formularContent: KokuDto.AbstractFormField<any>) => FormularContentStates;
        inputBindings?(instance: any, formularContent: KokuDto.AbstractFormField<any>): Record<string, any>;
        outputBindings?(instance: any, formularContent: KokuDto.AbstractFormField<any>): Record<string, any>;
      }
    >
  >;
  fieldSlotRegistry: Partial<
    Record<
      KokuDto.IFormFieldSlot['@type'] | string,
      {
        componentType: any;
        inputBindings?(instance: any, formularContent: KokuDto.IFormFieldSlot): Record<string, any>;
        outputBindings?(instance: any, formularContent: KokuDto.IFormFieldSlot): Record<string, any>;
      }
    >
  >;
  containerRegistry: Partial<
    Record<
      KokuDto.AbstractFormContainer['@type'] | string,
      {
        componentType: any;
        stateInitializer: (formularContent: KokuDto.AbstractFormContainer) => FormularContentStates;
        inputBindings?(instance: any, formularContent: KokuDto.AbstractFormContainer): Record<string, any>;
        outputBindings?(instance: any, formularContent: KokuDto.AbstractFormContainer): Record<string, any>;
      }
    >
  >;
}

export type FormularPluginFactory = (instance: FormularComponent) => FormularPlugin;

export const FORMULAR_PLUGIN = new InjectionToken<FormularPluginFactory | FormularPluginFactory[]>('Formular Plugins');

export interface FormularPlugin {
  onFormularLoaded?(formularData: KokuDto.FormViewDto): void;

  onSourceLoaded?(source: any): void;

  onSubmitError?(
    error: HttpErrorResponse,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
  ): boolean;

  destroy?(): void;
}

@Component({
  selector: 'formular',
  imports: [ContainerRendererComponent],
  templateUrl: './formular.component.html',
  styleUrl: './formular.component.css',
})
export class FormularComponent implements OnDestroy, OnChanges {
  httpClient = inject(HttpClient);
  toastService = inject(ToastService);
  formularPluginsConfig = inject(FORMULAR_PLUGIN, {
    optional: true,
  });

  formularUrl = input.required<string>();
  contentSetup = input.required<FormularContentSetup>();

  sourceUrl = input<string>();
  submitUrl = input<string>();
  submitMethod = input<string>();
  maxWidth = input<string | number>();
  buttonDockOutlet = input<OutletDirective>();
  headerTemplate = input<TemplateRef<any>>();
  fieldOverrides = input<FormularFieldOverride[]>([]);
  sourceOverrides = input<FormularSourceOverride[]>([]);
  context = input<Record<string, any>>();

  sourceLoading = signal(false);
  submitting = signal(false);
  dirty = signal(false);
  formularData = signal<KokuDto.FormViewDto | null>(null);

  fieldRegister = signal<FormularFieldRegistrationType>({});
  buttonRegister = signal<ButtonRegistrationType>({});
  containerRegister = signal<ContainerRegistrationType>({});
  layoutRegister = signal<LayoutRegistrationType>({});

  onSave = output<any>();

  source = {};
  private lastSourceSubscription: Subscription | undefined;
  private lastFormularSubscription: Subscription | undefined;
  private pluginInstances: FormularPlugin[] = [];

  constructor() {
    const pluginInstances = [];
    let pluginsConfig = this.formularPluginsConfig;
    if (pluginsConfig) {
      if (!Array.isArray(pluginsConfig)) {
        pluginsConfig = [pluginsConfig];
      }
      for (const currentPlugin of pluginsConfig || []) {
        pluginInstances.push(currentPlugin(this));
      }
    }
    this.pluginInstances = pluginInstances;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['formularUrl']) {
      this.loadFormular().subscribe(() => {
        this.loadSource();
      });
    } else if (changes['sourceUrl']) {
      this.loadSource();
    }
  }

  ngOnDestroy(): void {
    for (const currentPluginInstance of this.pluginInstances || []) {
      currentPluginInstance.destroy?.();
    }
  }

  private loadFormular() {
    return new Observable((subscriber) => {
      const formularUrlSnapshot = this.formularUrl();
      if (formularUrlSnapshot) {
        if (this.lastFormularSubscription && !this.lastFormularSubscription.closed) {
          this.lastFormularSubscription.unsubscribe();
        }
        this.lastFormularSubscription = this.httpClient.get<KokuDto.FormViewDto>(formularUrlSnapshot).subscribe({
          next: (formularData) => {
            this.formularData.set(formularData);
            if (formularData.contentRoot) {
              const root =
                this.contentSetup().containerRegistry[
                  formularData.contentRoot['@type'] as KokuDto.AbstractFormContainer['@type']
                ];
              if (root) {
                const formularStates = root.stateInitializer(formularData.contentRoot as any);
                this.fieldRegister.set(formularStates.fields);
                for (const fieldState of Object.values(formularStates.fields)) {
                  fieldState.fieldEventBus.subscribe((eventBody) => {
                    if (!fieldState.config.id) {
                      throw new Error(`missing field id for config ${fieldState.config}`);
                    }
                    const eventName = eventBody.eventName;
                    const eventPayload = eventBody.payload;
                    console.log(fieldState.config.id, eventName, eventPayload);

                    switch (eventName) {
                      case 'onInit': {
                        fieldState.instance = eventPayload;
                        break;
                      }
                      case 'onChange': {
                        this.setDirty();
                        fieldState.value.set(eventPayload);
                        const valuePath = fieldState.config.valuePath;
                        if (valuePath) {
                          set(this.source, valuePath, eventPayload);
                        }
                      }
                    }
                  });
                }
                this.buttonRegister.set(formularStates.buttons);
                for (const buttonState of Object.values(formularStates.buttons)) {
                  buttonState.buttonEventBus.subscribe((eventBody) => {
                    if (!buttonState.config.id) {
                      throw new Error(`missing button id for config ${buttonState.config}`);
                    }
                    const eventName = eventBody.eventName;
                    const eventPayload = eventBody.payload;
                    console.log(buttonState.config.id, eventName, eventPayload);
                    switch (eventName) {
                      case 'onClick': {
                        if (buttonState.config.buttonType === 'SUBMIT') {
                          this.submit().subscribe(() => {
                            for (const currentPostProcessingAction of buttonState.config.postProcessingActions || []) {
                              switch (currentPostProcessingAction['@type']) {
                                case 'reload': {
                                  this.loadSource();
                                  break;
                                }
                                default:
                                  throw new Error('Unknown PostProcessingAction');
                              }
                            }
                          });
                        }
                        break;
                      }
                    }
                  });
                }
                this.containerRegister.set(formularStates.containers);
              }
            }
            for (const currentPluginInstance of this.pluginInstances || []) {
              currentPluginInstance.onFormularLoaded?.(formularData);
            }
            subscriber.next(formularData);
            subscriber.complete();
          },
          error: (err) => {
            this.toastService.add('Fehler beim Laden der Daten! Versuchs später erneut!', 'error');
            subscriber.error(err);
          },
        });
      } else {
        subscriber.error('missing formularurl');
      }
    });
  }

  private afterSourceLoaded(source: any) {
    this.source = source;

    for (const sourceOverride of this.sourceOverrides() || []) {
      const valuePath = sourceOverride.path;
      const value = sourceOverride.value;
      if (valuePath && value !== undefined) {
        set(this.source, valuePath, value);
      }
    }

    const fieldInstancesSnapshot = this.fieldRegister();
    for (const value of Object.values(fieldInstancesSnapshot)) {
      const valuePathSnapshot = value.config.valuePath;
      const defaultValue = value.config.defaultValue;
      if (valuePathSnapshot) {
        const sourceOrDefaultValue = get(source, valuePathSnapshot.split('.'), defaultValue);
        value.value.set(sourceOrDefaultValue);
        set(this.source, valuePathSnapshot, sourceOrDefaultValue);
      }
    }

    for (const fieldOverride of this.fieldOverrides() || []) {
      const lookupField = fieldInstancesSnapshot[fieldOverride.fieldId];
      if (lookupField !== undefined) {
        if (fieldOverride.disable === true) {
          const disabledCausesSnapshot = lookupField.disabledCauses();
          disabledCausesSnapshot.add('fieldOverride');
          lookupField.disabledCauses.set(disabledCausesSnapshot);
        }
        const newValue = fieldOverride.value;
        if (newValue !== undefined) {
          lookupField.value.set(newValue);
          const valuePath = lookupField.config.valuePath;
          if (valuePath) {
            set(this.source, valuePath, newValue);
          }
        }
      } else {
        throw new Error('could not find field id ' + fieldOverride.fieldId);
      }
    }

    this.fieldRegister.set({ ...fieldInstancesSnapshot });

    for (const currentPluginInstance of this.pluginInstances || []) {
      currentPluginInstance.onSourceLoaded?.(source);
    }
  }

  private loadSource() {
    const sourceUrlSnapshot = this.sourceUrl();
    if (sourceUrlSnapshot) {
      this.sourceLoading.set(true);
      if (this.lastSourceSubscription && !this.lastSourceSubscription.closed) {
        this.lastSourceSubscription.unsubscribe();
      }
      this.lastSourceSubscription = this.httpClient.get(sourceUrlSnapshot).subscribe({
        next: (source) => {
          this.afterSourceLoaded(source);
        },
        error: () => {
          this.sourceLoading.set(false);
        },
        complete: () => {
          this.sourceLoading.set(false);
        },
      });
    } else {
      this.afterSourceLoaded({});
    }
  }

  submit() {
    if (this.submitting()) {
      throw new Error('submit is in progress');
    } else {
      return new Observable((observer) => {
        this.verifyNoFieldError().subscribe((noError) => {
          if (noError) {
            const submitUrl = this.submitUrl() || this.sourceUrl();
            if (!submitUrl) {
              throw new Error('target url is unresolvable');
            }
            this.submitting.set(true);

            this.requestSubmit(this.submitMethod() || 'POST', submitUrl, this.source).subscribe({
              next: (response: any) => {
                this.onSave.emit(response);
                this.toastService.add(`Erfolgreich gespeichert`, 'success');
                observer.next(response);
                observer.complete();
              },
              error: (error: any) => {
                if (error instanceof HttpErrorResponse) {
                  this.toastService.add(
                    `Es ist ein Fehler bei der Anfrage aufgetreten. ${error.status}: ${error.statusText}`,
                    'error',
                  );
                } else {
                  this.toastService.add(`Es ist ein unbekannter Fehler bei der Anfrage aufgetreten.`, 'error');
                }
                this.submitting.set(false);
                observer.error(error);
              },
              complete: () => {
                this.submitting.set(false);
                this.dirty.set(false);
                observer.complete();
              },
            });
          } else {
            observer.complete();
          }
        });
      });
    }
  }

  private verifyNoFieldError() {
    return new Observable<boolean>((subscriber) => {
      setTimeout(() => {
        let noError = true;
        for (const currentField of Object.values(this.fieldRegister())) {
          if (
            currentField.instance &&
            currentField.instance.instance &&
            typeof currentField.instance.instance.validate === 'function'
          ) {
            try {
              if (!currentField.instance.instance.validate()) {
                this.toastService.add('Überprüfe die Eingaben', 'warning');
                noError = false;
                this.tryFocus(currentField.instance.location);
                break;
              }
            } catch (error) {
              subscriber.error(error);
            }
          }
        }
        subscriber.next(noError);
        subscriber.complete();
      });
    });
  }

  private tryFocus(location: ElementRef) {
    if (location) {
      const nativeEl = location.nativeElement;
      if (nativeEl) {
        const focusableNodes = nativeEl.querySelectorAll(
          'a, button, input, textarea, select, details, [tabindex]:not([tabindex="-1"])',
        );
        if (focusableNodes) {
          const firstFocusable = focusableNodes[0];
          if (firstFocusable) {
            firstFocusable.focus();
          }
        }
      }
    }
  }

  private setDirty() {
    const dirtySnapshot = this.dirty();
    if (!dirtySnapshot) {
      this.dirty.set(true);
    }
  }

  requestSubmit(submitMethod: string, submitUrl: string, submitData: any): Observable<any> {
    return new Observable((observer) => {
      this.httpClient
        .request(submitMethod, submitUrl, {
          body: submitData,
        })
        .subscribe({
          next: (response) => {
            observer.next(response);
            observer.complete();
          },
          error: (error: HttpErrorResponse) => {
            let handled = false;
            for (const currentPluginInstance of this.pluginInstances || []) {
              const handledTmp =
                currentPluginInstance.onSubmitError?.(error, observer, submitMethod, submitUrl, submitData) || false;
              handled = handled || handledTmp;
            }
            if (!handled) {
              observer.error(error);
            }
          },
        });
    });
  }
}
