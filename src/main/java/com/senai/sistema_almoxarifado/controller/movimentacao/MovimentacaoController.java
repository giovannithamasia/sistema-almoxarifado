package com.senai.sistema_almoxarifado.controller.movimentacao;

import com.senai.sistema_almoxarifado.dto.movimentacao.MovimentacaoEstoqueDto;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.repository.UsuarioRepository;
import com.senai.sistema_almoxarifado.service.MovimentacaoEstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoEstoqueService service;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/movimentacaocadastrar")
    public String registrarMovimentacao(
            @Valid @ModelAttribute("movimentacaoDto") MovimentacaoEstoqueDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "tipo", required = false) String tipo,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String mensagemErroValidacao = bindingResult.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("mensagemErro", mensagemErroValidacao);

            recarregarListasNaTela(model);
            return "movimentacoes/listarmovimentacoes";
        }

        if (tipo == null || tipo.isBlank()) {
            model.addAttribute("mensagemErro", "O tipo de movimentação é obrigatório.");
            recarregarListasNaTela(model);
            return "movimentacoes/listarmovimentacoes";
        }

        // Usuário logado (responsável pela movimentação), vindo do Spring Security
        UsuarioEntity usuarioLogado = usuarioRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));

        try {
            if ("ENTRADA".equalsIgnoreCase(tipo)) {
                service.registrarEntrada(dto, usuarioLogado);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentação de entrada registrada com sucesso!");

            } else if ("SAIDA".equalsIgnoreCase(tipo)) {
                service.registrarSaida(dto, usuarioLogado);

                String mensagemAlerta = service.verificarAlertaEstoqueMinimo(dto.produtoId());
                if (mensagemAlerta != null) {
                    redirectAttributes.addFlashAttribute("alertaMinimo", "Movimentação registrada com sucesso! ATENÇÃO: " + mensagemAlerta);
                } else {
                    redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentação de saída registrada com sucesso!");
                }
            } else {
                model.addAttribute("mensagemErro", "Tipo de movimentação inválido.");
                recarregarListasNaTela(model);
                return "movimentacoes/listarmovimentacoes";
            }

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Quantidade insuficiente")) {
                model.addAttribute("erroQuantidade", e.getMessage());
            } else {
                model.addAttribute("mensagemErro", e.getMessage());
            }
            recarregarListasNaTela(model);
            return "movimentacoes/listarmovimentacoes";
        }

        return "redirect:/movimentacoes";
    }

    // usuarioLogado e isAdmin já são adicionados em todas as telas pelo UsuarioAdvice
    private void recarregarListasNaTela(Model model) {
        model.addAttribute("listaProdutos", service.listarProdutosCadastrados());
        model.addAttribute("listaMovimentacoes", service.listarHistoricoMovimentacoes());
    }
}