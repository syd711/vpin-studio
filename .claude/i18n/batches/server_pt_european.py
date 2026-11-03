# Server bundle: existing Brazilian Portuguese values -> European Portuguese (applied with server_pt_apply below)
# usage: python -I .claude/i18n/batches/server_pt_european.py
import io, os, re

PT = {
  "backup.job.running": "A criar a cópia de segurança de {0}…",
  "backup.restore.running": "A restaurar {0}…",
  "upload.job.title": "Envio de ficheiro",
  "upload.job.running": "A enviar {0}…",
  "upload.job.processing": "A processar {0}…",
  "install.job.running": "A instalar {0}…",
  "scan.job.running": "A analisar {0}…",
  "validate.job.running": "A validar {0}…",
  "error.file_not_found": "Ficheiro não encontrado: {0}",
  "status.processing": "A processar…",
  "component.update.running": "A atualizar {0}…",
  "media.upload.running": "A enviar conteúdo multimédia para {0}…",
  "media.delete.done": "Conteúdo multimédia eliminado: {0}",
  "media.convert.running": "A converter {0}…",
  "highscore.scan.running": "A ler as pontuações de {0}…",
  "recorder.job.title": "Gravador de ecrã ({0} jogos)",
  "recorder.ingame.title": "Gravador de ecrã no jogo",
  "recorder.status.launching_emulator": "A iniciar o emulador",
  "recorder.status.launching": "A iniciar “{0}”",
  "recorder.status.timeout": "Tempo esgotado à espera do emulador.",
  "recorder.status.recording": "A gravar “{0}”",
  "recorder.status.launching_frontend": "A iniciar o frontend",
  "puppack.install.title": "A instalar o PUP Pack “{0}”",
  "puppack.install.status": "A descompactar {0} de {1}",
  "dof.sync.title": "A sincronizar as configurações DOF",
  "dof.sync.status": "A extrair a pasta de configuração DOF {0}",
  "rom2altsound.title": 'A gerar o ALT sound para "{0}"',
  "rom2altsound.status.booting": "A iniciar a ROM {0}…",
  "rom2altsound.status.extracting": "A gravar o som {0} de {1}…",
  "rom2altsound.status.factory": "A ler as definições de fábrica de {0}…",
  "rom2altsound.status.decoding": "A descodificar a ROM de som de {0}…",
  "rom2altsound.status.retrying": "A gravar novamente o som {0} de {1}…",
  "rom2altsound.status.checking": "A verificar o som {0} de {1}…",
  "rom2altsound.status.packing": "A gravar o pacote ALT sound…",
  "rom2altsound.status.installing": "A instalar o ALT sound em {0}…",
  "vpxz.create.title": "A criar o .vpxz para “{0}”",
  "vpxz.create.calculating": "A calcular o tamanho de exportação de {0}",
  "vpxz.create.packing": "A empacotar {0}",
  "vpxz.create.copying": "A copiar o ficheiro vpxz para {0}",
  "server.update.downloading": "A transferir {0}… {1}%",
  "server.update.installing": "A instalar {0}…",
}

if __name__ == "__main__":
    p = os.path.join(r"C:\workspace\vpin-studio", "vpin-studio-server", "src", "main", "resources", "messages", "messages_pt.properties")
    t = io.open(p, encoding="utf-8", newline="").read()
    esc = lambda v: "".join(c if ord(c) < 128 else chr(92) + "u%04x" % ord(c) for c in v)
    for k, v in PT.items():
        t, n = re.subn("^" + re.escape(k) + "=.*$", lambda m: k + "=" + esc(v), t, count=1, flags=re.M)
        assert n == 1, k
    io.open(p, "w", encoding="utf-8", newline="").write(t)
    print("updated", len(PT), "pt values")
