"""
consolidar_projeto.py

Percorre o projeto (backend Java/Spring, frontend Angular, ou qualquer stack)
e gera um único arquivo .txt com o conteúdo de todos os arquivos relevantes,
no formato:

==================================================
ARQUIVO: caminho/relativo/do/arquivo.ext
==================================================
<conteúdo do arquivo>

Uso:
    python consolidar_projeto.py
    python consolidar_projeto.py --raiz ../metrotecnica-api --saida consolidado_api.txt
    python consolidar_projeto.py --raiz . --saida consolidado.txt --max-kb 300

Pensado para gerar o snapshot do projeto que você compartilha com o Claude
a cada sessão de trabalho (substitui o consolidar_projeto.py original do Flask).
"""

import argparse
import os
from pathlib import Path

# Pastas que nunca devem entrar no consolidado (build, dependências, VCS, etc.)
PASTAS_IGNORADAS = {
    ".git", ".idea", ".vscode", "__pycache__", ".mypy_cache", ".pytest_cache",
    "node_modules", "dist", "build", "target", "out",
    ".angular", ".gradle", ".mvn", "bin",
    "venv", ".venv", "env",
    "coverage", ".next", ".nuxt",
}

# Arquivos que nunca devem entrar (mesmo estando fora das pastas acima)
ARQUIVOS_IGNORADOS = {
    ".DS_Store", "Thumbs.db", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
    "mvnw", "mvnw.cmd", "gradlew", "gradlew.bat",
}

# Extensões de arquivo consideradas "código/config relevante".
# Ajuste essa lista conforme a stack do momento (Flask -> Java/Angular).
EXTENSOES_RELEVANTES = {
    # Backend Java / Spring
    ".java", ".xml", ".properties", ".yml", ".yaml", ".sql",
    # Frontend Angular / Web
    ".ts", ".html", ".scss", ".css", ".json",
    # Scripts e docs
    ".py", ".md", ".sh", ".gitignore", ".env.example",
}

# Arquivos sem extensão que ainda assim queremos incluir, se aparecerem
NOMES_SEM_EXTENSAO_RELEVANTES = {
    "Dockerfile", "Makefile",
}


def deve_incluir(caminho: Path, max_kb: int) -> bool:
    if caminho.name in ARQUIVOS_IGNORADOS:
        return False

    if caminho.suffix.lower() not in EXTENSOES_RELEVANTES and caminho.name not in NOMES_SEM_EXTENSAO_RELEVANTES:
        return False

    try:
        tamanho_kb = caminho.stat().st_size / 1024
    except OSError:
        return False

    if tamanho_kb > max_kb:
        return False

    return True


def coletar_arquivos(raiz: Path, max_kb: int):
    arquivos = []
    for dirpath, dirnames, filenames in os.walk(raiz):
        # Remove pastas ignoradas "in place" para o os.walk não descer nelas
        dirnames[:] = [d for d in dirnames if d not in PASTAS_IGNORADAS]

        for filename in sorted(filenames):
            caminho = Path(dirpath) / filename
            if deve_incluir(caminho, max_kb):
                arquivos.append(caminho)

    return sorted(arquivos)


def ler_conteudo(caminho: Path) -> str:
    try:
        return caminho.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        try:
            return caminho.read_text(encoding="latin-1")
        except Exception as e:
            return f"[ERRO AO LER ARQUIVO: {e}]"
    except Exception as e:
        return f"[ERRO AO LER ARQUIVO: {e}]"


def gerar_consolidado(raiz: str, saida: str, max_kb: int):
    raiz_path = Path(raiz).resolve()
    arquivos = coletar_arquivos(raiz_path, max_kb)

    linhas = []
    linhas.append(f"CONSOLIDADO DO PROJETO: {raiz_path.name}")
    linhas.append(f"TOTAL DE ARQUIVOS: {len(arquivos)}")
    linhas.append("=" * 50)
    linhas.append("")

    for caminho in arquivos:
        relativo = caminho.relative_to(raiz_path)
        conteudo = ler_conteudo(caminho)

        linhas.append("=" * 50)
        linhas.append(f"ARQUIVO: {relativo}")
        linhas.append("=" * 50)
        linhas.append("")
        linhas.append(conteudo)
        linhas.append("")

    texto_final = "\n".join(linhas)

    saida_path = Path(saida)
    saida_path.write_text(texto_final, encoding="utf-8")

    print(f"✅ Consolidado gerado: {saida_path.resolve()}")
    print(f"   Arquivos incluídos: {len(arquivos)}")
    print(f"   Tamanho final: {saida_path.stat().st_size / 1024:.1f} KB")


def main():
    parser = argparse.ArgumentParser(description="Consolida os arquivos do projeto em um único .txt")
    parser.add_argument("--raiz", default=".", help="Pasta raiz do projeto (padrão: pasta atual)")
    parser.add_argument("--saida", default="consolidado_projeto.txt", help="Nome do arquivo .txt de saída")
    parser.add_argument("--max-kb", type=int, default=500, help="Ignora arquivos maiores que esse tamanho em KB (padrão: 500)")
    args = parser.parse_args()

    gerar_consolidado(args.raiz, args.saida, args.max_kb)


if __name__ == "__main__":
    main()