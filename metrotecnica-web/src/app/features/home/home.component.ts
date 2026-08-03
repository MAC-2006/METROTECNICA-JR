import { Component, ElementRef, afterNextRender, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly sanitizer = inject(DomSanitizer);

  // Angular sanitiza qualquer [src] de iframe por padrão; isso "libera"
  // especificamente essa URL do Google Maps para ser embutida com segurança.
  protected readonly mapaUrl: SafeResourceUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
    'https://www.google.com/maps?q=Rua+Jos%C3%A9+Sutti,+54,+V%C3%A1rzea+Paulista,+SP&output=embed'
  );

  protected readonly stats = [
    { valor: '17+', rotulo: 'Anos de experiência' },
    { valor: '4', rotulo: 'Grandezas metrológicas' },
    { valor: 'RBC', rotulo: 'Acreditação Cgcre/INMETRO' },
    { valor: '24h', rotulo: 'Emissão de certificado' }
  ];

  // Cada serviço é rotulado pela grandeza física que ele mede — não por um
  // ícone decorativo. É a mesma unidade que aparece nos certificados emitidos.
  protected readonly grandezas = [
    {
      unidade: 'mm',
      titulo: 'Dimensional',
      descricao: 'Paquímetros, micrômetros, réguas, blocos-padrão, calibradores e instrumentos de altura.',
      imagem: 'https://commons.wikimedia.org/wiki/Special:FilePath/Messschieber.jpg?width=700',
      alt: 'Paquímetro medindo uma peça com precisão em milímetros'
    },
    {
      unidade: '°C',
      titulo: 'Temperatura e Umidade',
      descricao: 'Termômetros, termopares, data-loggers, câmaras climáticas, estufas e incubadoras.',
      imagem: 'https://commons.wikimedia.org/wiki/Special:FilePath/Laboratory_digital_thermometer.jpg?width=700',
      alt: 'Termômetro digital de laboratório em processo de calibração'
    },
    {
      unidade: 'bar',
      titulo: 'Pressão e Vácuo',
      descricao: 'Manômetros, vacuômetros, transmissores, pressostatos e calibradores de pressão.',
      imagem: 'https://commons.wikimedia.org/wiki/Special:FilePath/MAXIMATOR-High-Pressure-Manometer-01a.jpg?width=700',
      alt: 'Manômetro de alta pressão com mostrador de precisão'
    },
    {
      unidade: 'kg',
      titulo: 'Massa',
      descricao: 'Balanças analíticas e industriais, pesos-padrão e sistemas de pesagem rastreáveis.',
      imagem: 'https://commons.wikimedia.org/wiki/Special:FilePath/MassStandards_005.jpg?width=700',
      alt: 'Conjunto de pesos-padrão utilizados como referência metrológica'
    }
  ];

  // Sequência real do processo — a mesma que o sistema Metrotécnica executa
  // internamente (cadastro, comparação com padrões, PDF assinado, QR de validação).
  protected readonly processo = [
    {
      numero: '01',
      titulo: 'Solicitação',
      descricao: 'Você agenda a coleta ou envia o instrumento até o laboratório em Várzea Paulista.'
    },
    {
      numero: '02',
      titulo: 'Calibração',
      descricao: 'Comparação com padrões rastreáveis Cgcre/INMETRO, seguindo a instrução técnica de cada instrumento.'
    },
    {
      numero: '03',
      titulo: 'Certificado digital',
      descricao: 'Emissão do certificado em PDF, com assinatura eletrônica e QR code de validação.'
    },
    {
      numero: '04',
      titulo: 'Rastreabilidade',
      descricao: 'Histórico, vencimentos e reincidências acompanhados no painel do cliente.'
    }
  ];

  protected readonly credenciais = [
    {
      titulo: 'Acreditado RBC/INMETRO',
      descricao: 'Laboratório com acreditação segundo ABNT NBR ISO/IEC 17025 — rastreabilidade garantida.',
      icone: 'certificate'
    },
    {
      titulo: 'Certificados Digitais',
      descricao: 'Assinatura eletrônica válida (MP 2.200-2/2001) e QR code de autenticidade em cada certificado.',
      icone: 'shield'
    },
    {
      titulo: '+17 Anos de Experiência',
      descricao: 'Atendendo indústrias farmacêuticas, alimentícias, automotivas e metal-mecânicas.',
      icone: 'clock'
    }
  ];

  protected readonly contato = {
    endereco: 'Rua José Sutti, 54 – Jardim Maria de Fátima, Várzea Paulista/SP – CEP 13225-080',
    telefone: '(11) 4595-6307',
    email: 'contato@metrotecnica.com.br',
    whatsappLink: 'https://wa.me/551145956307?text=Olá!%20Gostaria%20de%20solicitar%20um%20orçamento%20de%20calibração.'
  };

  constructor() {
    afterNextRender(() => {
      this.initRevealAnimations();
      this.initActiveNavTracking();
    });
  }

  // Anima os elementos .reveal (cards, títulos de seção, etc.) conforme
  // entram na viewport, dando o efeito de "surgir" ao rolar a página.
  private initRevealAnimations(): void {
    const elements = this.host.nativeElement.querySelectorAll('.reveal');

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
          }
        }
      },
      { threshold: 0.15, rootMargin: '0px 0px -60px 0px' }
    );

    elements.forEach((el: Element) => observer.observe(el));
  }

  // Marca o link do menu correspondente à seção visível no momento,
  // dando feedback de onde o usuário está durante a rolagem suave.
  private initActiveNavTracking(): void {
    // Array.from(...) + cast em vez de querySelectorAll<HTMLAnchorElement>(...):
    // com isolatedModules ativo no tsconfig, o compilador do Angular (esbuild)
    // transpila arquivo por arquivo e não resolve com segurança generics
    // explícitos em métodos do DOM (TS2347). Isso contorna sem perder tipagem.
    const navLinks = Array.from(
      this.host.nativeElement.querySelectorAll('.topbar__nav a[href^="#"]')
    ) as HTMLAnchorElement[];
    if (!navLinks.length) return;

    const sectionIds = Array.from(navLinks)
      .map((link) => link.getAttribute('href')?.replace('#', ''))
      .filter((id): id is string => !!id);

    const sections = sectionIds
      .map((id) => this.host.nativeElement.querySelector(`#${id}`))
      .filter((el): el is Element => !!el);

    if (!sections.length) return;

    const setActive = (id: string | null) => {
      navLinks.forEach((link) => {
        const isActive = id !== null && link.getAttribute('href') === `#${id}`;
        link.classList.toggle('is-active', isActive);
      });
    };

    const navObserver = new IntersectionObserver(
      (entries) => {
        const visivel = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];

        if (visivel) {
          setActive(visivel.target.id);
        }
      },
      {
        // Faixa estreita no centro da tela: a seção "ativa" é a que
        // está cruzando o meio do viewport, não a que só apareceu na borda.
        rootMargin: '-45% 0px -45% 0px',
        threshold: 0
      }
    );

    sections.forEach((el) => navObserver.observe(el));
  }
}