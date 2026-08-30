// JavaScript para interatividade da Home

document.addEventListener('DOMContentLoaded', function() {
    // Dropdown do usuário
    const userToggle = document.querySelector('.user-toggle');
    const dropdownMenu = document.querySelector('.dropdown-menu');
    
    if (userToggle && dropdownMenu) {
        userToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
        });
        
        // Fechar dropdown ao clicar fora
        document.addEventListener('click', function() {
            dropdownMenu.style.display = 'none';
        });
        
        // Prevenir fechamento ao clicar dentro do dropdown
        dropdownMenu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }
});