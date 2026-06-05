import {
  booleanAttribute,
  Component,
  ComponentRef,
  computed,
  effect,
  forwardRef,
  input,
  signal,
  untracked,
} from '@angular/core';
import { FormularContent, FormularContentRegistry, FormularRuntime } from '../formular.component';
import { OutletDirective } from '../../portal/outlet.directive';
import { PortalDirective } from '../../portal/portal.directive';
import { DynamicRenderRecipe, KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { FORM_OUTLET, FormOutlet } from '../form-outlet';

@Component({
  selector: '[formular-content-renderer],formular-content-renderer',
  imports: [PortalDirective, KokuDynamicHostDirective, forwardRef(() => FormularContentRendererComponent)],
  templateUrl: './formular-content-renderer.component.html',
  styleUrl: 'formular-content-renderer.component.css',
})
export class FormularContentRendererComponent {
  readonly outlets = FORM_OUTLET;
  content = input.required<FormularContent>();
  runtime = input.required<FormularRuntime>();
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });
  contentRegistry = input.required<FormularContentRegistry>();
  buttonDockOutlet = input<OutletDirective>();
  placementOutlet = input<FormOutlet>(FORM_OUTLET.CONTENT);
  context = input<Record<string, any>>();

  enableDockedOutput = signal(false);
  dynamicRecipe = signal<DynamicRenderRecipe | null>(null);
  activeContent = computed(() => {
    const content = this.content();
    return this.runtime().contentConfig(content);
  });
  contentOverride = computed(() => {
    const alias = this.activeContent().alias;
    return alias ? this.runtime().contentOverridesByAlias().get(alias) : undefined;
  });
  private readonly recipeIdentity = computed(
    () => {
      const content = this.activeContent();
      return {
        id: content.id,
        factory: this.contentRegistry()[content['@type']],
        runtime: this.runtime(),
      };
    },
    {
      equal: (previous, current) =>
        previous.id === current.id && previous.factory === current.factory && previous.runtime === current.runtime,
    },
  );

  constructor() {
    effect(() => {
      const contentSnapshot = this.activeContent();
      this.enableDockedOutput.set(
        Boolean((contentSnapshot as KokuDto.KokuFormButton).dockable && this.buttonDockOutlet()),
      );
    });
    effect(() => {
      const identity = this.recipeIdentity();
      try {
        this.dynamicRecipe.set(untracked(() => this.createDynamicRecipe(identity.id, identity.factory)));
      } catch (error) {
        identity.runtime.failInitialization(error);
        throw error;
      }
    });
  }

  captureInstance(instance: ComponentRef<unknown>) {
    const contentId = this.activeContent().id;
    if (!contentId) {
      return;
    }
    const component = instance.instance as { validate?: () => boolean };
    const host = instance.location.nativeElement as HTMLElement;
    this.runtime().attachInstance(contentId, {
      validate: typeof component.validate === 'function' ? () => component.validate!() : undefined,
      focus: () => {
        const target = host.querySelector<HTMLElement>(
          'a, button, input, textarea, select, details, [tabindex]:not([tabindex="-1"])',
        );
        target?.focus();
      },
    });
  }

  private createDynamicRecipe(
    contentId: string | undefined,
    recipeFactory: NonNullable<FormularContentRegistry[string]> | undefined,
  ) {
    if (!contentId) {
      throw new Error('Form content requires a stable id');
    }
    if (!recipeFactory) {
      throw new Error(`No recipe registered for content type: ${this.activeContent()['@type']}`);
    }

    const recipe = recipeFactory({
      id: contentId,
      content: this.activeContent,
      override: this.contentOverride,
      runtime: this.runtime(),
      loading: this.loading,
      submitting: this.submitting,
      contentRegistry: this.contentRegistry,
      buttonDockOutlet: this.buttonDockOutlet,
      enableDockedOutput: this.enableDockedOutput,
      placementOutlet: this.placementOutlet,
      context: this.context,
    });
    const handle = this.runtime().resolveContent(contentId, recipe.control);
    return recipe.render(handle);
  }
}
