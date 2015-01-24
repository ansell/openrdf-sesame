interface YASQE_Instance extends JQuery {

	getValue(): string;

	setValue(query: string): void;

	refresh(): void;

	save(): void;

	toTextArea(): HTMLElement;

	getWrapperElement(): Element;
}

interface YASQE_Config {
	consumeShareLink(): any;
}

interface YASQE_Static {
	
	fromTextArea(textArea: HTMLElement, config: YASQE_Config): YASQE_Instance;
}

declare module "yasqe" {
	export = YASQE;
}

declare var YASQE: YASQE_Static;