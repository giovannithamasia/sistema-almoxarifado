package com.senai.sistema_almoxarifado.sessoes;

import jakarta.servlet.http.HttpSession;

public class SessaoUtil {

    private static final String USUARIO_LOGADO = "usuarioLogado";

    private SessaoUtil() {
    }

    public static void RegistrarSessao(HttpSession session, SessaoDto sessaoDto) {
        session.setAttribute(USUARIO_LOGADO, sessaoDto);
    }

    public static SessaoDto ObterSessao(HttpSession session) {
        Object usuarioLogado = session.getAttribute(USUARIO_LOGADO);

        if (usuarioLogado == null) {
            return null;
        }

        return (SessaoDto) usuarioLogado;
    }

    public static void RemoverSessao(HttpSession session) {
        session.removeAttribute(USUARIO_LOGADO);
        session.invalidate();
    }
}

