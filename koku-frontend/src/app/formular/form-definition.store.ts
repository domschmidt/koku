import { signal, Signal, WritableSignal } from '@angular/core';
import type { FormularContent } from './formular.component';

interface PlacementGraph {
  parentByChild: Map<string, string>;
  childrenByParent: Map<string, string[]>;
}

export class FormDefinitionStore {
  private readonly contents = new Map<string, WritableSignal<FormularContent | undefined>>();
  private readonly outlets = new Map<string, WritableSignal<readonly string[]>>();
  private readonly contentTypes = new Map<string, string>();
  private readonly emptyOutlet = signal<readonly string[]>([]);
  private contentIdsSnapshot = new Set<string>();

  setFormView(formView: KokuDto.FormViewDto) {
    const contents = (formView.contents ?? {}) as Record<string, FormularContent>;
    this.validate(formView, contents);
    this.validateStableContentTypes(contents);
    this.updateContentSignals(contents);
    this.updateOutletSignals(this.createOutletPlacements(formView));
    this.contentIdsSnapshot = new Set(Object.keys(contents));
  }

  private validateStableContentTypes(contents: Record<string, FormularContent>): void {
    for (const [id, content] of Object.entries(contents)) {
      const knownType = this.contentTypes.get(id);
      if (knownType && knownType !== content['@type']) {
        throw new Error(`Content type cannot change for stable id ${id}`);
      }
    }
  }

  private updateContentSignals(contents: Record<string, FormularContent>): void {
    for (const [id, contentSignal] of this.contents) {
      const nextContent = contents[id];
      contentSignal.set(nextContent);
    }
    for (const [id, content] of Object.entries(contents)) {
      this.contentTypes.set(id, content['@type']);
      this.contentSignal(id).set(content);
    }
  }

  private createOutletPlacements(formView: KokuDto.FormViewDto): Map<string, string[]> {
    const placements = new Map<string, string[]>();
    for (const placement of formView.placements ?? []) {
      this.addOutletPlacement(placements, placement);
    }
    return placements;
  }

  private addOutletPlacement(placements: Map<string, string[]>, placement: KokuDto.FormPlacementDto): void {
    const key = this.outletKey(placement.parentId!, placement.outlet!);
    const childIds = placements.get(key) ?? [];
    childIds.push(placement.childId!);
    placements.set(key, childIds);
  }

  private updateOutletSignals(placements: Map<string, string[]>): void {
    for (const [key, outletSignal] of this.outlets) {
      outletSignal.set(placements.get(key) ?? []);
    }
    for (const [key, childIds] of placements) {
      this.outletSignal(key).set(childIds);
    }
  }

  reset() {
    for (const content of this.contents.values()) {
      content.set(undefined);
    }
    for (const outlet of this.outlets.values()) {
      outlet.set([]);
    }
    this.contents.clear();
    this.outlets.clear();
    this.contentTypes.clear();
    this.contentIdsSnapshot.clear();
  }

  content(id: string | undefined): FormularContent | undefined {
    return id ? this.contentSignal(id)() : undefined;
  }

  contentSignal(id: string): WritableSignal<FormularContent | undefined> {
    let content = this.contents.get(id);
    if (!content) {
      content = signal<FormularContent | undefined>(undefined);
      this.contents.set(id, content);
    }
    return content;
  }

  childIds(parentId: string | undefined, outlet: string): Signal<readonly string[]> {
    if (!parentId) {
      return this.emptyOutlet;
    }
    return this.outletSignal(this.outletKey(parentId, outlet));
  }

  contentIds(): ReadonlySet<string> {
    return this.contentIdsSnapshot;
  }

  updateContent(id: string, updater: (content: FormularContent) => FormularContent) {
    const content = this.content(id);
    if (!content) {
      throw new Error(`Content not found: ${id}`);
    }
    const updated = updater(content);
    if (updated.id !== id) {
      throw new Error(`Content id cannot change: ${id} != ${updated.id ?? '<undefined>'}`);
    }
    if (updated['@type'] !== content['@type']) {
      throw new Error(`Content type cannot change for stable id ${id}`);
    }
    this.contentSignal(id).set(updated);
  }

  private outletSignal(key: string): WritableSignal<readonly string[]> {
    let outlet = this.outlets.get(key);
    if (!outlet) {
      outlet = signal<readonly string[]>([], {
        equal: (previous, current) =>
          previous.length === current.length && previous.every((id, index) => id === current[index]),
      });
      this.outlets.set(key, outlet);
    }
    return outlet;
  }

  private outletKey(parentId: string, outlet: string) {
    return `${parentId}\u0000${outlet}`;
  }

  private validate(formView: KokuDto.FormViewDto, contents: Record<string, FormularContent>) {
    this.validateRootContent(formView, contents);
    this.validateContentIds(contents);
    const placementGraph = this.createPlacementGraph(formView, contents);
    this.validateContentPlacement(formView, contents, placementGraph.parentByChild);
    this.validateReachability(formView.rootId!, contents, placementGraph.childrenByParent);
  }

  private validateRootContent(formView: KokuDto.FormViewDto, contents: Record<string, FormularContent>) {
    if (!formView.rootId || !contents[formView.rootId]) {
      throw new Error(`Form root content is missing: ${formView.rootId ?? '<undefined>'}`);
    }
  }

  private validateContentIds(contents: Record<string, FormularContent>) {
    for (const [id, content] of Object.entries(contents)) {
      if (!content.id) {
        throw new Error(`Form content requires an id: ${id}`);
      }
      if (content.id !== id) {
        throw new Error(`Form content id does not match its registry key: ${id} != ${content.id}`);
      }
    }
  }

  private createPlacementGraph(
    formView: KokuDto.FormViewDto,
    contents: Record<string, FormularContent>,
  ): PlacementGraph {
    const placementGraph: PlacementGraph = {
      parentByChild: new Map<string, string>(),
      childrenByParent: new Map<string, string[]>(),
    };
    for (const placement of formView.placements ?? []) {
      this.addPlacementToGraph(placementGraph, formView, contents, placement);
    }
    return placementGraph;
  }

  private addPlacementToGraph(
    placementGraph: PlacementGraph,
    formView: KokuDto.FormViewDto,
    contents: Record<string, FormularContent>,
    placement: KokuDto.FormPlacementDto,
  ) {
    if (!placement.parentId || !placement.outlet || !placement.childId) {
      throw new Error('Form placement requires parentId, outlet and childId');
    }
    if (!contents[placement.parentId]) {
      throw new Error(`Placement parent content not found: ${placement.parentId}`);
    }
    if (!contents[placement.childId]) {
      throw new Error(`Placement child content not found: ${placement.childId}`);
    }
    if (placement.childId === formView.rootId) {
      throw new Error('Form root content cannot be placed as a child');
    }
    if (placementGraph.parentByChild.has(placement.childId)) {
      throw new Error(`Content is placed more than once: ${placement.childId}`);
    }
    placementGraph.parentByChild.set(placement.childId, placement.parentId);
    const children = placementGraph.childrenByParent.get(placement.parentId) ?? [];
    children.push(placement.childId);
    placementGraph.childrenByParent.set(placement.parentId, children);
  }

  private validateContentPlacement(
    formView: KokuDto.FormViewDto,
    contents: Record<string, FormularContent>,
    parentByChild: Map<string, string>,
  ) {
    for (const id of Object.keys(contents)) {
      if (id !== formView.rootId && !parentByChild.has(id)) {
        throw new Error(`Content has no placement: ${id}`);
      }
    }
  }

  private validateReachability(
    rootId: string,
    contents: Record<string, FormularContent>,
    childrenByParent: Map<string, string[]>,
  ) {
    const visiting = new Set<string>();
    const visited = new Set<string>();
    const visit = (id: string) => {
      if (visiting.has(id)) {
        throw new Error(`Form placement cycle detected at: ${id}`);
      }
      if (visited.has(id)) {
        return;
      }
      visiting.add(id);
      for (const childId of childrenByParent.get(id) ?? []) {
        visit(childId);
      }
      visiting.delete(id);
      visited.add(id);
    };
    visit(rootId);
    if (visited.size !== Object.keys(contents).length) {
      throw new Error('Form contains content that is unreachable from the root');
    }
  }
}
